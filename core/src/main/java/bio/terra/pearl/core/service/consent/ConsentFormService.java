package bio.terra.pearl.core.service.consent;

import bio.terra.pearl.core.dao.consent.ConsentFormDao;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.consent.ConsentForm;
import bio.terra.pearl.core.service.ImmutableEntityService;
import bio.terra.pearl.core.service.VersionedEntityService;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConsentFormService extends ImmutableEntityService<ConsentForm, ConsentFormDao> implements VersionedEntityService<ConsentForm> {
    public ConsentFormService(ConsentFormDao dao) {
        super(dao);
    }

    public Optional<ConsentForm> findByStableId(String stableId, int version) {
        return dao.findByStableId(stableId, version);
    }

    @Transactional
    public void deleteByPortalId(UUID portalId) {
        List<ConsentForm> forms = dao.findByPortalId(portalId);
        for (ConsentForm form : forms) {
            delete(form.getId(), new HashSet<>());
        }
    }

    @Transactional
    public ConsentForm createNewVersion(AdminUser adminUser, UUID portalId, ConsentForm consentForm) {
        // TODO check user permissions
        ConsentForm newConsent = new ConsentForm();
        BeanUtils.copyProperties(consentForm, newConsent, "id", "version", "createdAt", "lastUpdatedAt");
        newConsent.setPortalId(portalId);
        int nextVersion = dao.getNextVersion(consentForm.getStableId());
        newConsent.setVersion(nextVersion);
        ConsentForm newForm =  create(newConsent);
        logger.info("Created new ConsentForm version:  stableId: {}, version: {}");
        return newForm;
    }
}
