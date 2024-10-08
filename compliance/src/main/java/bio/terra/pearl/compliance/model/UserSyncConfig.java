package bio.terra.pearl.compliance.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

@Getter @Setter @SuperBuilder @NoArgsConstructor
public class UserSyncConfig {

    private Collection<PersonInScope> peopleInScope;

    private String vantaBaseUrl;

    private String vantaClientId;

    private String vantaClientSecret;

    private String slackToken;

    private String slackChannel;

    // gs: path to json file that contains message ids to ignore
    private String messageIdsBucketPath;

    /**
     * For reasons unknown, there are some resource ids that vanta
     * flags as nonexistent when updating metadata, even though
     * these resource ids are present when we query vanta for
     * all resources for a given integration id.  This method
     * returns all such resource ids so that we can avoid metadata
     * update exceptions.  These ids are easy to identify in the logs,
     * as they show up in the "message" field returned from the
     * update resources API call, like so: "message" : "Some resource IDs do not exist: A, B, C, ..."
     * The key is the integration id, and the value is the list of resource ids
     * to ignore for that integration id.
     */
    public Set<String> getResourceIdsToIgnore(String integrationId) {
        if (resourceIdsToIgnore != null) {
            return resourceIdsToIgnore.getOrDefault(integrationId, Collections.emptySet());
        } else {
            return Collections.emptySet();
        }
    }

    private Map<String, Set<String>> resourceIdsToIgnore;
}
