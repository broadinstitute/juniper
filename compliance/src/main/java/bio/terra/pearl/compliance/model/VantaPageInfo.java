package bio.terra.pearl.compliance.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter @Setter @SuperBuilder @NoArgsConstructor
public class VantaPageInfo {

    private String startCursor;

    private String endCursor;

    private boolean hasNextPage;

    private boolean hasPreviousPage;

    @Override
    public String toString() {
        return "VantaPageInfo{" +
                "startCursor='" + startCursor + '\'' +
                ", endCursor='" + endCursor + '\'' +
                ", hasNextPage=" + hasNextPage +
                ", hasPreviousPage=" + hasPreviousPage +
                '}';
    }
}
