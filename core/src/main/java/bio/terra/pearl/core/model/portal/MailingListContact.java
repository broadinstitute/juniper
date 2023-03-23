package bio.terra.pearl.core.model.portal;

import bio.terra.pearl.core.model.BaseEntity;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class MailingListContact extends BaseEntity {
    private String name;
    private String email;
    /** Someone can join the mailing list for either a portal, or a study within a portal */
    private UUID portalEnvironmentId;
    private UUID studyEnvironmentId;
    /** if someone joins a mailing list while they are signed in, track it */
    private UUID participantUserId;
}
