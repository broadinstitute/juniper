package bio.terra.pearl.core.model.audit;

import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import com.azure.core.annotation.Get;
import lombok.Getter;
import lombok.Setter;

/**
 * dto class for indicating one of the 3 types of responsible users for changes.
 * this class is built to ensure that exactly one of the three possibilities will be specified
 */
@Getter
public class ResponsibleEntity {
    private AdminUser adminUser;
    private ParticipantUser participantUser;
    private String systemProcess;
    private Boolean anonymousUser;
    public ResponsibleEntity(AdminUser user) {
        if (user == null) {
            throw new IllegalArgumentException("user cannot be null");
        }
        this.adminUser = user;
    }
    public ResponsibleEntity(ParticipantUser user) {
        if (user == null) {
            throw new IllegalArgumentException("user cannot be null");
        }
        this.participantUser = user;
    }
    public ResponsibleEntity(String systemProcess) {
        if (systemProcess == null) {
            throw new IllegalArgumentException("system process cannot be null");
        }
        this.systemProcess = systemProcess;
    }
    public ResponsibleEntity() {
        this.anonymousUser = true;
    }
}
