package bio.terra.pearl.core.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter @Setter @SuperBuilder
public class Enrollee extends BaseEntity {
    private UUID participantUserId;
    private UUID studyEnvironmentId;
    private String shortcode;
    @Builder.Default
    private boolean withdrawn = false;
}
