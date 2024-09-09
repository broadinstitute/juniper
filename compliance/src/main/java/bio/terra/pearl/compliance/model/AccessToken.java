package bio.terra.pearl.compliance.model;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter @Setter @SuperBuilder @NoArgsConstructor
public class AccessToken {

    private String access_token, token_type;

    private int expires_in;
}
