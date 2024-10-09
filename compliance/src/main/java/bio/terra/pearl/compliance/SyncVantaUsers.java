package bio.terra.pearl.compliance;

import bio.terra.pearl.compliance.exception.RateLimitException;
import bio.terra.pearl.compliance.exception.VantaUpdateException;
import bio.terra.pearl.compliance.model.AccessToken;
import bio.terra.pearl.compliance.model.CloudEventPayload;
import bio.terra.pearl.compliance.model.DuoAccount;
import bio.terra.pearl.compliance.model.GithubAccount;
import bio.terra.pearl.compliance.model.GsuiteAccount;
import bio.terra.pearl.compliance.model.JamfComputer;
import bio.terra.pearl.compliance.model.JiraAccount;
import bio.terra.pearl.compliance.model.PersonInScope;
import bio.terra.pearl.compliance.model.PubsubIdsToIgnore;
import bio.terra.pearl.compliance.model.SlackUser;
import bio.terra.pearl.compliance.model.SyncResult;
import bio.terra.pearl.compliance.model.UpdateVantaMetadata;
import bio.terra.pearl.compliance.model.UserSyncConfig;
import bio.terra.pearl.compliance.model.VantaCredentials;
import bio.terra.pearl.compliance.model.VantaIntegration;
import bio.terra.pearl.compliance.model.VantaObject;
import bio.terra.pearl.compliance.model.VantaPerson;
import bio.terra.pearl.compliance.model.VantaResults;
import bio.terra.pearl.compliance.model.VantaResultsResponse;
import bio.terra.pearl.compliance.model.WorkdayAccount;
import com.google.cloud.functions.CloudEventsFunction;
import com.google.cloud.spring.storage.GoogleStorageResource;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.spring.secretmanager.SecretManagerTemplate;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.slack.api.Slack;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import io.cloudevents.CloudEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.WritableResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * Run this as a main() or as a cloud function to set
 * various vanta integrations to use only the team members
 * needed for our audit.  When running locally, set the
 * GOOGLE_CLOUD_PROJECT and VANTA_CONFIG_SECRET environment vars.
 *
 * Because this job takes longer than the max pubsub
 * ack timeout, pubsub will retry messages.  We keep a list
 * of messages to tell us which ones we've seen before
 * and can safely ignore.  We store it in cloud storage and it's
 * possible that with multiple jobs running at once there may
 * be race condition overwrites, but that isn't something
 * that matters for this scheduled job.
 */
@Slf4j
@SpringBootApplication
public class SyncVantaUsers implements CommandLineRunner, CloudEventsFunction {

    public static final String JIRA_INTEGRATION_ID = "jira";
    public static final String SLACK_INTEGRATION_ID = "slack";
    public static final String GITHUB_INTEGRATION_ID = "github";
    public static final String JAMF_INTEGRATION_ID = "jamf";
    public static final String DUO_INTEGRATION_ID = "duo";
    public static final String GSUITE_INTEGRATION_ID = "gsuiteadmin";

    private Gson gson = newGson();

    @Autowired
    private SecretManagerTemplate secretManagerTemplate;

    @Value("#{environment.VANTA_CONFIG_SECRET}")
    private String vantaConfigSecret;

    @Autowired
    private Storage storage;

    private UserSyncConfig userSyncConfig;

    private Collection<PersonInScope> peopleInScope = new ArrayList<>();

    private SpringApplication app = new SpringApplicationBuilder(SyncVantaUsers.class).web(WebApplicationType.NONE).bannerMode(Banner.Mode.OFF).build();

    /**
     * Runs the sync, as dispatched from cloud function.
     */
    @Override
    public void accept(CloudEvent event) throws Exception {
        String payload = new String(event.getData().toBytes(), StandardCharsets.UTF_8);
        app.run(payload);
    }

    private static Gson newGson() {
        return new GsonBuilder().serializeNulls().create();
    }

    private GoogleStorageResource getStorageResource() {
        return new GoogleStorageResource(this.storage, userSyncConfig.getMessageIdsBucketPath());
    }

    public static void main(String[] args) {
        try {
            new SpringApplicationBuilder(SyncVantaUsers.class)
                    .web(WebApplicationType.NONE)
                    .bannerMode(Banner.Mode.OFF)
                    .build()
                    .run(newGson().toJson(new CloudEventPayload()));
        } catch (Exception e) {
            log.error("Could not run sync", e);
        }
    }

    private UserSyncConfig loadConfig() {
        if (userSyncConfig == null) {
            userSyncConfig = gson.fromJson(secretManagerTemplate.getSecretString(vantaConfigSecret), UserSyncConfig.class);
            peopleInScope.clear();
            peopleInScope.addAll(userSyncConfig.getPeopleInScope());
        }
        return userSyncConfig;
    }

    private boolean shouldRun(CloudEventPayload payload) throws IOException {
        return payload == null || StringUtils.isBlank(payload.getMessageId()) || !getMessageIdsToIgnore().contains(payload.getMessageId());
    }

    @Override
    public void run(String... args) throws Exception {
        CloudEventPayload pubsubPayload = gson.fromJson(args[0], CloudEventPayload.class);
        log.info("Starting vanta sync app in response to message id {}", pubsubPayload.getMessageId());
        loadConfig();
        final Set<String> pubsubIdsProcessed = new HashSet<>(getMessageIdsToIgnore());
        log.info("List of message ids to ignore has {} items", pubsubIdsProcessed.size());
        if (shouldRun(pubsubPayload)) {
            if (StringUtils.isNotBlank(pubsubPayload.getMessageId())) {
                pubsubIdsProcessed.add(pubsubPayload.getMessageId());
                saveMessageIdsToIgnore(new PubsubIdsToIgnore(pubsubIdsProcessed));
            }
            Instant start = Instant.now();
            SyncResult syncSummary = syncVantaAccounts();
            Duration duration = Duration.between(start, Instant.now());

            log.info("Vanta sync completed after {}m.  Posting update to slack.", duration.toMinutes());

            // only post to slack if something has changed
            if (syncSummary.hasVantaDataChanged()) {
                try (Slack slack = Slack.getInstance()) {
                    ChatPostMessageResponse response = slack.methods(userSyncConfig.getSlackToken()).chatPostMessage(req -> req
                            .channel(userSyncConfig.getSlackChannel())
                            .text("Vanta sync complete after " + duration.toMinutes() + "m.\n" + syncSummary.getTextSummary()));

                    if (!response.isOk()) {
                        log.info("Slack message returned {} {}", response.getMessage(), response.getError());
                    }
                }
            } else {
                log.info("No updates made it vanta, skipping slack notification.");
            }
        } else {
            if (pubsubIdsProcessed.contains(pubsubPayload.getMessageId())) {
                log.info("Skipping duplicate processing of pubsub message {}", pubsubPayload.getMessageId());
            }
        }
    }

    private Function<ClientResponse, Mono<? extends Throwable>> get429StatusHander() {
        return res -> {
            List<String> header = res.headers().header("Retry-After");
            int delayInSeconds;
            if (!header.isEmpty()) {
                delayInSeconds = Integer.parseInt(header.get(0));
            } else {
                delayInSeconds = 61;
            }
            log.info("Vanta rate limit exceeded; waiting for {}s", delayInSeconds);
            return res.bodyToMono(String.class).map(msg -> new RateLimitException(msg, delayInSeconds));
        };
    }

    /**
     * Given a ready-to-retrieve rest client, fetches
     * all data in the results.data[] array and converts
     * it to the appropriate types
     */
    private <T extends VantaObject> Collection<T> collectAllData(WebClient wc, ParameterizedTypeReference<VantaResultsResponse<T>> type) {
        final AtomicReference<VantaResults<T>> results = new AtomicReference<>();
        AtomicReference<List<T>> elements = new AtomicReference<>(new ArrayList<>());
        AtomicInteger cursorCount = new AtomicInteger(0);

        while (cursorCount.get() == 0 || results.get().getPageInfo().isHasNextPage()) {
            log.info("Getting {} cursor number {}", type.getType().getTypeName(), cursorCount);

            VantaResultsResponse<T> response = (wc.get().uri(uriBuilder -> {
                        if (cursorCount.get() > 0) {
                            uriBuilder.queryParam("pageCursor", results.get().getPageInfo().getEndCursor());
                        }
                        uriBuilder.queryParam("pageSize", 100);
                        return uriBuilder.build();
                    }).retrieve()).onStatus(HttpStatus.TOO_MANY_REQUESTS::equals, get429StatusHander())
                    .bodyToMono(type)
                    .retryWhen(getRetry()).block();
            cursorCount.incrementAndGet();
            results.set(response.getResults());

            if (response != null) {
                if (response.getResults() != null) {
                    elements.get().addAll(response.getResults().getData());
                }
            } else {
                break;
            }
        }
        return elements.get();
    }

    private WebClient.Builder getWebClientForUrl(String accessToken, String url) {
        return WebClient.builder().defaultHeaders(h -> {
            h.setBearerAuth(accessToken);
            h.setContentType(MediaType.APPLICATION_JSON);
        }).baseUrl(url);
    }

    private RetryBackoffSpec getRetry() {
        return Retry.fixedDelay(5, Duration.ofSeconds(61)).filter(RateLimitException.class::isInstance);
    }

    private WebClient getWebClientForIntegration(String accessToken, String integrationId, String resourceKind) {
        return getWebClientForUrl(accessToken, userSyncConfig.getVantaBaseUrl() + "v1/integrations/" + integrationId + "/resource-kinds/" + resourceKind + "/resources").build();
    }

    private List<VantaIntegration> getIntegrations() {
        List<VantaIntegration> integrationsToSync = new ArrayList<>();
        integrationsToSync.add(new VantaIntegration(GSUITE_INTEGRATION_ID, "GsuiteUser", new ParameterizedTypeReference<VantaResultsResponse<GsuiteAccount>>() {},
                userSyncConfig.getResourceIdsToIgnore(GSUITE_INTEGRATION_ID)));
        integrationsToSync.add(new VantaIntegration(DUO_INTEGRATION_ID, "DuoAccount", new ParameterizedTypeReference<VantaResultsResponse<DuoAccount>>() {},
                userSyncConfig.getResourceIdsToIgnore(DUO_INTEGRATION_ID)));
        integrationsToSync.add(new VantaIntegration(JAMF_INTEGRATION_ID, "JamfManagedComputer", new ParameterizedTypeReference<VantaResultsResponse<JamfComputer>>() {},
                userSyncConfig.getResourceIdsToIgnore(JAMF_INTEGRATION_ID)));
        integrationsToSync.add(new VantaIntegration(JIRA_INTEGRATION_ID, "JiraAccount", new ParameterizedTypeReference<VantaResultsResponse<JiraAccount>>() {},
                userSyncConfig.getResourceIdsToIgnore(JIRA_INTEGRATION_ID)));
        integrationsToSync.add(new VantaIntegration(SLACK_INTEGRATION_ID, "SlackAccount", new ParameterizedTypeReference<VantaResultsResponse<SlackUser>>() {},
                userSyncConfig.getResourceIdsToIgnore(SLACK_INTEGRATION_ID)));
        integrationsToSync.add(new VantaIntegration(GITHUB_INTEGRATION_ID, "GithubAccount", new ParameterizedTypeReference<VantaResultsResponse<GithubAccount>>() {},
                userSyncConfig.getResourceIdsToIgnore(GITHUB_INTEGRATION_ID)));
        integrationsToSync.add(new VantaIntegration("workday", "WorkdayHrUser", new ParameterizedTypeReference<VantaResultsResponse<WorkdayAccount>>() {},
                userSyncConfig.getResourceIdsToIgnore("workday")));
        return integrationsToSync;
    }

    public SyncResult syncVantaAccounts() {
        SyncResult syncResult = new SyncResult();
        WebClient wc = WebClient.builder().baseUrl(userSyncConfig.getVantaBaseUrl() + "oauth/token").build();
        AccessToken vantaToken= wc.post().bodyValue(new VantaCredentials("client_credentials", "vanta-api.all:read vanta-api.all:write", userSyncConfig.getVantaClientId(), userSyncConfig.getVantaClientSecret()))
                .header("Content-Type", "application/json").retrieve().bodyToMono(AccessToken.class).block();
        WebClient.builder().defaultHeaders(h -> {
            h.setBearerAuth(vantaToken.getAccessToken());
            h.setContentType(MediaType.APPLICATION_JSON);
        });

        // go through each integration and find scope changes that need to be made.
        int resourceBatchSize = 50;
        List<VantaIntegration> integrationsToSync = getIntegrations();

        Collection<VantaPerson> vantaPeople = collectAllData(getWebClientForUrl(vantaToken.getAccessToken(), userSyncConfig.getVantaBaseUrl() + "v1/people").build(), new ParameterizedTypeReference<>() {});

        // associate each vanta person with people in scope so we can keep track of computers that should be in scope
        peopleInScope.forEach(personInScope -> {
            List<VantaPerson> matchingVantaPerson = vantaPeople.stream().filter(vantaPerson -> vantaPerson.getEmailAddress().equalsIgnoreCase(personInScope.getEmail())).toList();
            if (matchingVantaPerson.size() == 1) {
                personInScope.setVantaPerson(matchingVantaPerson.getFirst());
            } else if (matchingVantaPerson.size() > 1) {
                log.error("Found {} vanta people for {}", matchingVantaPerson.size(), personInScope.getFullName());
            } else {
                log.warn("Could not find any matching vanta people for {}", personInScope.getFullName());
            }
        });

        for (VantaIntegration vantaIntegration : integrationsToSync) {
            String integrationId = vantaIntegration.getIntegrationId();
            log.debug("Synchronizing {}", integrationId);
            String resourceKind = vantaIntegration.getResourceKind();
            ParameterizedTypeReference resultsResponseType = vantaIntegration.getResultsResponseType();

            Collection<VantaObject> vantaObjects = collectAllData(vantaToken.getAccessToken(), integrationId, resourceKind, resultsResponseType);
            log.debug("Found {} {} objects.", vantaObjects.size(), integrationId);

            Collection<VantaObject> objectsToUpdate = new HashSet<>();
            Collection<String> idsRemovedFromScope = new HashSet<>();
            Collection<String> idsAddedToScope = new HashSet<>();

            // in bulk, update people who aren't in scope but should be
            vantaObjects.stream().filter(vantaObject -> !vantaIntegration.shouldIgnoreResource(vantaObject.getResourceId()) && vantaObject.shouldBeInScope(peopleInScope) && !vantaObject.isInScope()).forEach(objectToMarkInScope -> {
                objectsToUpdate.add(objectToMarkInScope);
                syncResult.setHasVantaDataChanged(true);
                idsAddedToScope.add(objectToMarkInScope.getSimpleId());
                if (objectsToUpdate.size() == resourceBatchSize) {
                    setInScope(vantaToken.getAccessToken(), objectsToUpdate, true, integrationId, resourceKind);
                    objectsToUpdate.clear();
                }
            });
            if (!objectsToUpdate.isEmpty()) {
                setInScope(vantaToken.getAccessToken(), objectsToUpdate, true, integrationId, resourceKind);
                objectsToUpdate.clear();
            }

            // in bulk, update people who are in scope but who shouldn't be
            vantaObjects.stream().filter(vantaObject -> !vantaIntegration.shouldIgnoreResource(vantaObject.getResourceId()) && !vantaObject.shouldBeInScope(peopleInScope) && vantaObject.isInScope()).forEach(objectToMarkOutOfScope -> {
                objectsToUpdate.add(objectToMarkOutOfScope);
                idsRemovedFromScope.add(objectToMarkOutOfScope.getSimpleId());
                if (objectsToUpdate.size() == resourceBatchSize) {
                    setInScope(vantaToken.getAccessToken(), objectsToUpdate, false, integrationId, resourceKind);
                    objectsToUpdate.clear();
                }
            });
            if (!objectsToUpdate.isEmpty()) {
                setInScope(vantaToken.getAccessToken(), objectsToUpdate, false, integrationId, resourceKind);
                objectsToUpdate.clear();
            }

            String msg = "";
            if (idsAddedToScope.isEmpty() && idsRemovedFromScope.isEmpty()) {
                msg = String.format("No changes to scope in %s", integrationId);
                log.info(msg);
                syncResult.appendToSummary(msg + "\n");
            } else {
                if (idsAddedToScope.isEmpty()) {
                    msg = String.format("No objects added to scope in %s", integrationId);
                } else {
                    msg = String.format("Added the following %s objects to scope in %s: %s", idsAddedToScope.size(), integrationId,
                            String.join(",",idsAddedToScope));
                }
                log.info(msg);
                syncResult.appendToSummary(msg + "\n");

                if (idsRemovedFromScope.isEmpty()) {
                    msg = String.format("No objects removed from scope in %s", integrationId);
                } else {
                    msg = String.format("Removed the following %s objects from scope in %s: %s", idsRemovedFromScope.size(), integrationId,
                            String.join(",", idsRemovedFromScope));
                }
                log.info(msg);
                syncResult.appendToSummary(msg +"\n");
            }
        }
        return syncResult;
    }

    private <T extends VantaObject> Collection<T> collectAllData(String accessToken, String integrationId, String resourceKind, ParameterizedTypeReference<VantaResultsResponse<T>> resultsResponseClass) {
        WebClient wc = getWebClientForIntegration(accessToken, integrationId, resourceKind);
        return collectAllData(wc, resultsResponseClass);
    }

    private void setInScope(String accessTokeen, Collection<VantaObject> vantaObjects, boolean isInScope, String integrationId, String resourceKind) {
        log.debug("Updating {} {} objects to scope={}", vantaObjects.size(), integrationId, isInScope);
        UpdateVantaMetadata updateMetadata = new UpdateVantaMetadata();

        for (VantaObject vantaObject : vantaObjects) {
            updateMetadata.add(vantaObject.getResourceId(), isInScope);
        }
        try {
            String updateResult = getWebClientForIntegration(accessTokeen, integrationId, resourceKind)
                    .patch().bodyValue(updateMetadata).retrieve().onStatus(HttpStatus.TOO_MANY_REQUESTS::equals, get429StatusHander())
                    .onStatus(HttpStatus.UNPROCESSABLE_ENTITY::equals, res -> res.bodyToMono(String.class).map(VantaUpdateException::new))
                    .bodyToMono(String.class).retryWhen(getRetry()).block();
            log.info("Updated {} {} objects to {} with response {}", updateMetadata.size(), integrationId, isInScope, updateResult);
        } catch (VantaUpdateException e) {
            log.warn("Could not change scope to {} on some of {} {} objects due to {}", isInScope, integrationId, updateMetadata.size(), e.getMessage());
        }
    }

    /**
     * Loads the message ids to ignore from the gs path indicated in the config secret.
     */
    private Set<String> getMessageIdsToIgnore() throws IOException {
        GoogleStorageResource gcsResource = getStorageResource();
        if (!gcsResource.bucketExists()) {
            throw new RuntimeException("Bucket " + gcsResource.getBucketName() + " not found");
        }
        if (!gcsResource.exists()) {
            log.debug("Creating {}", gcsResource.getBlobName());
            Blob blob = gcsResource.createBlob();
            log.debug("Created blob {}", blob.getName());
            saveMessageIdsToIgnore(new PubsubIdsToIgnore());
        }
        String contentsAsString = StreamUtils.copyToString(gcsResource.getInputStream(), Charset.defaultCharset());
        return gson.fromJson(contentsAsString, PubsubIdsToIgnore.class).getIdsToIgnore();
    }

    private void saveMessageIdsToIgnore(PubsubIdsToIgnore idsToIgnore) throws IOException {
        GoogleStorageResource gcsResource = getStorageResource();
        try (OutputStream os = ((WritableResource) gcsResource).getOutputStream()) {
            os.write(gson.toJson(idsToIgnore).getBytes());
            os.flush();
        }
        log.debug("Wrote {} items as list of pubsub ids to ignore", idsToIgnore.getIdsToIgnore().size());
    }

}

