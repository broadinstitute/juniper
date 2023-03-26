package bio.terra.pearl.core.service.portal;

import bio.terra.pearl.core.dao.portal.MailingListContactDao;
import bio.terra.pearl.core.model.portal.MailingListContact;
import bio.terra.pearl.core.service.CrudService;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class MailingListContactService extends CrudService<MailingListContact, MailingListContactDao> {
    public MailingListContactService(MailingListContactDao dao) {
        super(dao);
    }

    public List<MailingListContact> findByPortalEnv(UUID portalEnvId) {
        return dao.findByPortalEnv(portalEnvId);
    }
}
