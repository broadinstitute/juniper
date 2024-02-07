package bio.terra.pearl.core.model.audit;

import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import com.azure.core.annotation.Get;
import lombok.Getter;
import lombok.Setter;

@Getter
public class ResponsibleEntity {
    private AdminUser adminUser;
    private ParticipantUser participantUser;
    private String systemProcess;
    public ResponsibleEntity(AdminUser user) {
        this.adminUser = user;
    }
    public ResponsibleEntity(ParticipantUser user) {
        this.participantUser = user;
    }
    public ResponsibleEntity(String systemProcess) {
        this.systemProcess = systemProcess;
    }
}
