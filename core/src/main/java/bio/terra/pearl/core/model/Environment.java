package bio.terra.pearl.core.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

@Getter @Setter @SuperBuilder
@NoArgsConstructor
public class Environment extends BaseEntity {
    private EnvironmentName name;
    @Builder.Default
    private Set<ParticipantUser> participantUsers = new HashSet<>();
}
