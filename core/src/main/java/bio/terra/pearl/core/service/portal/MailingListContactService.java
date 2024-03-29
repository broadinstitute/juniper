package bio.terra.pearl.core.service.portal;

import bio.terra.pearl.core.dao.portal.MailingListContactDao;
import bio.terra.pearl.core.model.portal.MailingListContact;
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

    @Transactional
    public List<MailingListContact> bulkCreate(UUID portalEnvId, List<MailingListContact> contacts) {
        findByPortalEnv(portalEnvId).forEach(existing ->
                contacts.removeIf(contact -> contact.getEmail().equals(existing.getEmail())));

        contacts.forEach(contact -> contact.setPortalEnvironmentId(portalEnvId));
        dao.bulkCreate(contacts);
        return contacts;
    }

    @Transactional
    public void deleteByPortalEnvId(UUID portalEnvId) {
        dao.deleteByPortalEnvId(portalEnvId);
    }
}
