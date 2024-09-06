package bio.terra.pearl.compliance.compliance.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.Collection;

@Getter @Setter @SuperBuilder @NoArgsConstructor
public class UserSyncConfig {

    private Collection<PersonInScope> peopleInScope;

    private String vantaBaseUrl;

    private String vantaClientId;

    private String vantaClientSecret;

    private String slackToken;

    private String slackChannel;
}
