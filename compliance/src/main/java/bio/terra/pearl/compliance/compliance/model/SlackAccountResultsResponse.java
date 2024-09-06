package bio.terra.pearl.compliance.compliance.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter @Setter @SuperBuilder @NoArgsConstructor
public class SlackAccountResultsResponse extends VantaResultsResponse<SlackUser> {

}
