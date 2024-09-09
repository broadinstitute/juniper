package bio.terra.pearl.compliance;

import bio.terra.pearl.compliance.exception.RateLimitException;
import bio.terra.pearl.compliance.exception.VantaUpdateException;
import bio.terra.pearl.compliance.model.AccessToken;
import bio.terra.pearl.compliance.model.GithubAccountResultsResponse;
import bio.terra.pearl.compliance.model.JiraAccountResultsResponse;
import bio.terra.pearl.compliance.model.PersonInScope;
import bio.terra.pearl.compliance.model.SlackAccountResultsResponse;
import bio.terra.pearl.compliance.model.UpdateVantaMetadata;
import bio.terra.pearl.compliance.model.UserSyncConfig;
import bio.terra.pearl.compliance.model.VantaCredentials;
import bio.terra.pearl.compliance.model.VantaIntegration;
import bio.terra.pearl.compliance.model.VantaObject;
import bio.terra.pearl.compliance.model.VantaResults;
import bio.terra.pearl.compliance.model.VantaResultsResponse;
import bio.terra.pearl.compliance.model.WorkdayAccountResultsResponse;
import com.google.cloud.functions.CloudEventsFunction;
import com.google.cloud.spring.secretmanager.SecretManagerTemplate;
import com.google.gson.Gson;
import com.slack.api.Slack;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import io.cloudevents.CloudEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

@Slf4j
@SpringBootApplication
public class SyncVantaUsers implements CommandLineRunner, CloudEventsFunction {

    @Autowired
    private SecretManagerTemplate secretManagerTemplate;

    private UserSyncConfig userSyncConfig;

    @Value("#{environment.PORT}")
    private Integer port;

    @Value("#{environment.VANTA_CONFIG_SECRET}")
    private String vantaConfigSecret;

    private String vantaBasePath;

    private String vantaClientId;

    private String vantaSecret;

    Collection<PersonInScope> peopleInScope = new ArrayList<>();

    public static void main(String[] args) {
        /*
        String cloudFunctionPort = System.getenv("PORT");

        if (StringUtils.isNotBlank(cloudFunctionPort)) {
            System.setProperty("server.port", cloudFunctionPort);
        }
         */

        new SpringApplicationBuilder(SyncVantaUsers.class)
                .web(WebApplicationType.NONE)
                .bannerMode(Banner.Mode.OFF)
                .run(args);
        log.info("Synchronization complete");
    }

    @Override
    public void accept(CloudEvent event) throws Exception {
        try {
            new SpringApplicationBuilder(SyncVantaUsers.class)
                    .web(WebApplicationType.NONE)
                    .bannerMode(Banner.Mode.OFF).run("");
            log.info("Synchronization complete");
        } catch (Exception e) {
            log.error("Vanta sync failed", e);
        }
    }

    @Override
    public void run(String... args) throws Exception {
        userSyncConfig = new Gson().fromJson(secretManagerTemplate.getSecretString(vantaConfigSecret), UserSyncConfig.class);
        vantaBasePath = userSyncConfig.getVantaBaseUrl();
        vantaClientId = userSyncConfig.getVantaClientId();
        vantaSecret = userSyncConfig.getVantaClientSecret();
        peopleInScope = userSyncConfig.getPeopleInScope();
        Instant start = Instant.now();
        String summaryMessage = syncVantaAccounts();
        Duration duration = Duration.between(start, Instant.now());

        Slack slack = Slack.getInstance(); Slack.getInstance();
        ChatPostMessageResponse response = slack.methods(userSyncConfig.getSlackToken()).chatPostMessage(req -> req
                .channel(userSyncConfig.getSlackChannel())
                .text("Vanta sync complete after " + duration.toMinutes() + "m.\n" + summaryMessage));

        if (!response.isOk()) {
            log.info("Slack message returned {} {}", response.getMessage(), response.getError());
        }
    }

    /**
     * Given a ready-to-retrieve rest client, fetches
     * all data in the results.data[] array, converting
     * it to dataClass, iterating through all pages
     * to build a complete list
     */
    private <T extends VantaObject> Collection<T> collectAllData(WebClient wc, Class<? extends VantaResultsResponse<T>> clazz) {
        final AtomicReference<VantaResults<T>> results = new AtomicReference<>();
        Set<T> elements = new HashSet<>();
        AtomicInteger cursorCount = new AtomicInteger(0);

        while (cursorCount.get() == 0 || results.get().getPageInfo().isHasNextPage()) {
            log.debug("Getting cursor number {}", cursorCount);
            VantaResultsResponse<T> response = wc.get().uri(uriBuilder -> {
                if (cursorCount.get() > 0) {
                    uriBuilder.queryParam("pageCursor", results.get().getPageInfo().getEndCursor());
                }
                uriBuilder.queryParam("pageSize", 100);
                return uriBuilder.build();
            }).retrieve()
                    .onStatus(HttpStatus.TOO_MANY_REQUESTS::equals, res -> {
                        List<String> header = res.headers().header("Retry-After");
                        Integer delayInSeconds;
                        if (!header.isEmpty()) {
                            delayInSeconds = Integer.valueOf(header.get(0));
                        } else {
                            delayInSeconds = 60;
                        }
                        log.debug("Vanta rate limit exceeded; waiting for {}s with {} objects", delayInSeconds, elements.size());
                        try {
                            Thread.sleep(Duration.ofSeconds(delayInSeconds));
                        } catch (InterruptedException e) {
                            log.error("Interrupted while sleeping in response to rate limit ");
                        }
                        return res.bodyToMono(String.class).map(msg -> new RateLimitException(msg, delayInSeconds));
                    })
                    .bodyToMono(clazz)
                    .retryWhen(Retry.withThrowable(throwableFlux -> {
                        return throwableFlux.filter(t -> t instanceof RateLimitException).map(t -> {
                            RateLimitException re = (RateLimitException) t;
                            return Retry.fixedDelay(1, re.getRetryAfterDelayDuration());
                        });
                    })).block();

            cursorCount.incrementAndGet();
            if (response != null) {
                elements.addAll(response.getResults().getData());
                results.set(response.getResults());
            } else {
                break;
            }
        }
        return elements;
    }

    private WebClient getWebClientForIntegration(String accessToken, String integrationId, String resourceKind) {
        WebClient.Builder webClientBuilder = WebClient.builder().defaultHeaders(h -> {
            h.setBearerAuth(accessToken);
            h.setContentType(MediaType.APPLICATION_JSON);
        });

        return webClientBuilder.baseUrl(vantaBasePath + "v1/integrations/" + integrationId + "/resource-kinds/" + resourceKind + "/resources").build();
    }

    public String syncVantaAccounts() {
        StringBuilder summary = new StringBuilder();
        WebClient wc = WebClient.builder().baseUrl(vantaBasePath + "oauth/token").build();
        AccessToken vantaToken= wc.post().bodyValue(new VantaCredentials("client_credentials", "vanta-api.all:read vanta-api.all:write", vantaClientId, vantaSecret))
                .header("Content-Type", "application/json").retrieve().bodyToMono(AccessToken.class).block();

        WebClient.builder().defaultHeaders(h -> {
            h.setBearerAuth(vantaToken.getAccess_token());
            h.setContentType(MediaType.APPLICATION_JSON);
        });

        // go through each integration and find scope changes that need to be made.
        int resourceBatchSize = 50;
        Collection<? extends VantaObject> vantaObjects = new HashSet<>();
        List<VantaIntegration> integrationsToSync = new ArrayList<>();
        integrationsToSync.add(new VantaIntegration("jira", "JiraAccount", JiraAccountResultsResponse.class, Set.of("669a8422865ac5731466a323", "66c629d278231843aec354ff", "669a8431865ac5731469ed44",
                "669a842e865ac5731469663b", "669a8427865ac5731467c5d0", "669a8431865ac5731469e994")));
        integrationsToSync.add(new VantaIntegration("slack", "SlackAccount", SlackAccountResultsResponse.class, Set.of("66a14ed517f6ad4ce8ea8e7f","66a14ee917f6ad4ce8ef12ff","66c74e0878231843aea7b330")));
        integrationsToSync.add(new VantaIntegration("github", "GithubAccount", GithubAccountResultsResponse.class,
                Set.of("66831806bb4f7b57d3b1b260", "66831804bb4f7b57d3b15f0c", "66831809bb4f7b57d3b24ecf", "66831803bb4f7b57d3b122b7", "66831802bb4f7b57d3b0c4d3",
                        "6683180abb4f7b57d3b2ac3c", "66831809bb4f7b57d3b23dc1", "668de80159091c1ee96b8e4a", "66831808bb4f7b57d3b2144b")));
        integrationsToSync.add(new VantaIntegration("workday", "WorkdayHrUser", WorkdayAccountResultsResponse.class, Collections.emptySet()));

        for (VantaIntegration vantaIntegration : integrationsToSync) {
            String integrationId = vantaIntegration.getIntegrationId();
            log.debug("Synchronizing {}", integrationId);
            String resourceKind = vantaIntegration.getResourceKind();
            Class resultsResponseClass = vantaIntegration.getResultsResponseClass();

            vantaObjects = collectAllData(vantaToken.getAccess_token(), integrationId, resourceKind, resultsResponseClass);
            log.debug("Found {} {} objects.", vantaObjects.size(), integrationId);

            Collection<VantaObject> objectsToUpdate = new HashSet<>();
            Collection<String> idsRemovedFromScope = new HashSet<>();
            Collection<String> idsAddedToScope = new HashSet<>();

            // in bulk, update people who aren't in scope but should be
            vantaObjects.stream().filter(vantaObject -> !vantaIntegration.shouldIgnoreResource(vantaObject.getResourceId()) && vantaObject.shouldBeInScope(peopleInScope) && !vantaObject.isInScope()).forEach(objectToMarkInScope -> {
                objectsToUpdate.add(objectToMarkInScope);
                idsAddedToScope.add(objectToMarkInScope.getSimpleId());
                if (objectsToUpdate.size() == resourceBatchSize) {
                    setInScope(vantaToken.getAccess_token(), objectsToUpdate, true, integrationId, resourceKind);
                    objectsToUpdate.clear();
                }
            });
            if (!objectsToUpdate.isEmpty()) {
                setInScope(vantaToken.getAccess_token(), objectsToUpdate, true, integrationId, resourceKind);
                objectsToUpdate.clear();
            }

            // in bulk, update people who are in scope but who shouldn't be
            vantaObjects.stream().filter(vantaObject -> !vantaIntegration.shouldIgnoreResource(vantaObject.getResourceId()) && !vantaObject.shouldBeInScope(peopleInScope) && vantaObject.isInScope()).forEach(objectToMarkOutOfScope -> {
                objectsToUpdate.add(objectToMarkOutOfScope);
                idsRemovedFromScope.add(objectToMarkOutOfScope.getSimpleId());
                if (objectsToUpdate.size() == resourceBatchSize) {
                    setInScope(vantaToken.getAccess_token(), objectsToUpdate, false, integrationId, resourceKind);
                    objectsToUpdate.clear();
                }
            });
            if (!objectsToUpdate.isEmpty()) {
                setInScope(vantaToken.getAccess_token(), objectsToUpdate, false, integrationId, resourceKind);
                objectsToUpdate.clear();
            }

            String msg = "";
            if (idsAddedToScope.isEmpty() && idsRemovedFromScope.isEmpty()) {
                msg = String.format("No changes to scope in %s", integrationId);
                log.info(msg);
                summary.append(msg).append("\n");
            } else {
                if (idsAddedToScope.isEmpty()) {
                    msg = String.format("No objects added to scope in %s", integrationId);
                } else {
                    msg = String.format("Added the following %s objects to scope in %s: %s", idsAddedToScope.size(), integrationId,
                            StringUtils.join(idsAddedToScope, ","));
                }
                log.info(msg);
                summary.append(msg).append("\n");

                if (idsRemovedFromScope.isEmpty()) {
                    msg = String.format("No objects removed from scope in %s", integrationId);
                } else {
                    msg = String.format("Removed the following %s objects from scope in %s: %s", idsRemovedFromScope.size(), integrationId,
                            StringUtils.join(idsRemovedFromScope, ","));
                }
                log.info(msg);
                summary.append(msg).append("\n");
            }
        }
        return summary.toString();
    }

    private <T extends VantaObject> Collection<T> collectAllData(String accessToken, String integrationId, String resourceKind, Class<? extends VantaResultsResponse<T>> resultsResponseClass) {
        WebClient wc = getWebClientForIntegration(accessToken, integrationId, resourceKind);
        return collectAllData(wc, resultsResponseClass);
    }

    private void setInScope(String accessTokeen, Collection<VantaObject> vantaObjects, boolean isInScope, String integrationId, String resourceKind) {
        log.debug("Updating {} {} objects to scope={}", vantaObjects.size(), integrationId, isInScope);
        UpdateVantaMetadata updateMetadata = new UpdateVantaMetadata();
        updateMetadata.setInScope(isInScope);
        vantaObjects.forEach(vantaObject -> updateMetadata.addResourceId(vantaObject.getResourceId()));
        try {
            String updateResult = getWebClientForIntegration(accessTokeen, integrationId, resourceKind)
                    .patch().bodyValue(updateMetadata).retrieve().onStatus(HttpStatus.UNPROCESSABLE_ENTITY::equals, res ->
                            res.bodyToMono(String.class).map(msg -> new VantaUpdateException(msg))
                    ).bodyToMono(String.class).block();
            log.debug("Updated {} {} objects to {} with response {}", updateMetadata.getResourceIds().size(), integrationId, isInScope, updateResult);
        } catch (VantaUpdateException e) {
            log.warn("Could not change scope to {} on some of {} {} objects due to {}", isInScope, integrationId, updateMetadata.getResourceIds().size(), e.getMessage());
        }
    }
}
