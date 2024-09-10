package bio.terra.pearl.compliance.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter @Setter @SuperBuilder
public class VantaCredentials {

    private String grant_type;

    private String scope;

    private String client_id;

    private String client_secret;

    public VantaCredentials(String grant_type, String scope, String client_id, String secret) {
        this.grant_type = grant_type;
        this.scope = scope;
        this.client_id = client_id;
        this.client_secret = secret;
    }


}
