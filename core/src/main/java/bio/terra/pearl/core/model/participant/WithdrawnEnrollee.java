package bio.terra.pearl.core.model.participant;

import bio.terra.pearl.core.model.BaseEntity;
import java.util.UUID;

import bio.terra.pearl.core.model.study.StudyEnvAttached;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * A withdrawn enrollee can no longer be included in any study views, their information is only preserved
 * for compliance purposes.  The withdrawal process should be treated as irreversible.
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class WithdrawnEnrollee extends BaseEntity implements StudyEnvAttached {
  private String shortcode;
  private UUID studyEnvironmentId;
  /** JSON of the Enrollee object and children that need to e saved for compliance */
  private String enrolleeData;
  /** JSON of the ParticipantUser object needed to be saved for compliance */
  private String userData;
  private EnrolleeWithdrawalReason reason;
  private String note;
}
