package bio.terra.pearl.compliance.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@ToString
public class PubsubIdsToIgnore {

    private Set<String> idsToIgnore;

    public PubsubIdsToIgnore(Set<String> idsToIgnore) {
        this.idsToIgnore = idsToIgnore;
    }
}
