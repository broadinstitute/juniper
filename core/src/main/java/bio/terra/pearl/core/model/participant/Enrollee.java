package bio.terra.pearl.core.model.participant;

import bio.terra.pearl.core.model.BaseEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter @Setter @SuperBuilder
@NoArgsConstructor
public class Enrollee extends BaseEntity {
    private UUID participantUserId;
    private UUID studyEnvironmentId;
    private String shortcode;
    @Builder.Default
    private boolean withdrawn = false;
}
