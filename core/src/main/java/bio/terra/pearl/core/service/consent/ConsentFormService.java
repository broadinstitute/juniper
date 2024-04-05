package bio.terra.pearl.core.service.consent;

import bio.terra.pearl.core.dao.consent.ConsentFormDao;
import bio.terra.pearl.core.model.consent.ConsentForm;
import bio.terra.pearl.core.model.survey.Answer;
import bio.terra.pearl.core.service.VersionedEntityService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Service
public class ConsentFormService extends VersionedEntityService<ConsentForm, ConsentFormDao> {
    public ConsentFormService(ConsentFormDao dao) {
        super(dao);
    }

    @Transactional
    public void deleteByPortalId(UUID portalId) {
        List<ConsentForm> forms = dao.findByPortalId(portalId);
        for (ConsentForm form : forms) {
            delete(form.getId(), new HashSet<>());
        }
    }

    public List<ConsentForm> findByPortalId(UUID portalId) {
        return dao.findByPortalId(portalId);
    }

    public List<ConsentForm> findByStableIdNoContent(String stableId, UUID portalId) {
        return dao.findByStableIdNoContent(stableId, portalId);
    }

    @Transactional
    public ConsentForm createNewVersion(UUID portalId, ConsentForm consentForm) {
        ConsentForm newConsent = new ConsentForm();
        BeanUtils.copyProperties(consentForm, newConsent, "id", "version", "createdAt", "lastUpdatedAt", "publishedVersion");
        newConsent.setPortalId(portalId);
        int nextVersion = dao.getNextFormVersion(consentForm.getStableId(), portalId);
        newConsent.setVersion(nextVersion);
        ConsentForm newForm =  create(newConsent);
        logger.info("Created new ConsentForm version:  stableId: {}, version: {}");
        return newForm;
    }

    public int getNextVersion(String stableId, UUID portalId) {
        return dao.getNextFormVersion(stableId, portalId);
    }
}
