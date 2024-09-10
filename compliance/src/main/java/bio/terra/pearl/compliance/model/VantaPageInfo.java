package bio.terra.pearl.compliance.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter @Setter @SuperBuilder @NoArgsConstructor @ToString
public class VantaPageInfo {

    private String startCursor;

    private String endCursor;

    private boolean hasNextPage;

    private boolean hasPreviousPage;
}
