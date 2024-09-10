package bio.terra.pearl.core.service.portal;

import bio.terra.pearl.core.dao.portal.MailingListContactDao;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.portal.MailingListContact;
import bio.terra.pearl.core.service.DataAuditedService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import bio.terra.pearl.core.service.ParticipantDataAuditedService;
import bio.terra.pearl.core.service.workflow.ParticipantDataChangeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MailingListContactService extends ParticipantDataAuditedService<MailingListContact, MailingListContactDao> {
    public MailingListContactService(MailingListContactDao dao, ParticipantDataChangeService participantDataChangeService, ObjectMapper objectMapper) {
        super(dao, participantDataChangeService, objectMapper);
    }

    public List<MailingListContact> findByPortalEnv(UUID portalEnvId) {
        return dao.findByPortalEnv(portalEnvId);
    }
    public Optional<MailingListContact> findByPortalEnv(UUID portalEnvId, String emailAddress) {
        return dao.findByPortalEnv(portalEnvId, emailAddress);
    }

    @Transactional
    public List<MailingListContact> bulkCreate(UUID portalEnvId, List<MailingListContact> contacts, DataAuditInfo auditInfo) {
        // remove contacts from the submitted list if they already exist in this environment
        findByPortalEnv(portalEnvId).forEach(existing ->
                contacts.removeIf(contact -> contact.getEmail().equals(existing.getEmail())));

        contacts.forEach(contact -> contact.setPortalEnvironmentId(portalEnvId));
        bulkCreate(contacts, auditInfo);
        return contacts;
    }

    @Transactional
    public void deleteByPortalEnvId(UUID portalEnvId) {
        dao.deleteByPortalEnvId(portalEnvId);
    }
}
