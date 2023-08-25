package bio.terra.pearl.core.service.portal;

import bio.terra.pearl.core.dao.portal.MailingListContactDao;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.portal.MailingListContact;
import bio.terra.pearl.core.model.workflow.DataChangeRecord;
import bio.terra.pearl.core.service.ImmutableEntityService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MailingListContactService extends ImmutableEntityService<MailingListContact, MailingListContactDao> {
    public MailingListContactService(MailingListContactDao dao) {
        super(dao);
    }

    public List<MailingListContact> findByPortalEnv(UUID portalEnvId) {
        return dao.findByPortalEnv(portalEnvId);
    }
    public Optional<MailingListContact> findByPortalEnv(UUID portalEnvId, String emailAddress) {
        return dao.findByPortalEnv(portalEnvId, emailAddress);
    }

    public void deleteByPortalEnvId(UUID portalEnvId) {
        dao.deleteByPortalEnvId(portalEnvId);
    }
}
