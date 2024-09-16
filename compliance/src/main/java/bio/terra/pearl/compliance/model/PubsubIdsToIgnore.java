package bio.terra.pearl.compliance.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class PubsubIdsToIgnore {

    private Set<String> idsToIgnore = new HashSet<>();

    public PubsubIdsToIgnore(Set<String> idsToIgnore) {
        this.idsToIgnore = idsToIgnore;
    }
}
