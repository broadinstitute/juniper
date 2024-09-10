package bio.terra.pearl.compliance.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter @Setter @SuperBuilder
public class VantaCredentials {

    @JsonProperty("grant_type")
    private String grantType;

    private String scope;

    @JsonProperty("client_id")
    private String clientId;

    @JsonProperty("client_secret")
    private String clientSecret;

    public VantaCredentials(String grantType, String scope, String clientId, String secret) {
        this.grantType = grantType;
        this.scope = scope;
        this.clientId = clientId;
        this.clientSecret = secret;
    }


}
