package bio.terra.pearl.core.service.consent;

import bio.terra.pearl.core.dao.consent.ConsentFormDao;
import bio.terra.pearl.core.dao.i18n.LanguageTextDao;
import bio.terra.pearl.core.model.consent.ConsentForm;
import bio.terra.pearl.core.model.i18n.LanguageText;
import bio.terra.pearl.core.service.VersionedEntityService;

import java.util.*;

import bio.terra.pearl.core.service.survey.SurveyParseUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Service
public class ConsentFormService extends VersionedEntityService<ConsentForm, ConsentFormDao> {
    private final LanguageTextDao languageTextDao;

    public ConsentFormService(ConsentFormDao dao, LanguageTextDao languageTextDao) {
        super(dao);
        this.languageTextDao = languageTextDao;
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
    @Override
    public ConsentForm create(ConsentForm consentForm) {
        ConsentForm newForm = dao.create(consentForm);

        // parse the consent content to get the titles and create the language texts
        Map<String, String> parsedTitles = SurveyParseUtils.parseSurveyTitle(newForm.getContent(), newForm.getName());
        List<LanguageText> texts = SurveyParseUtils.titlesToLanguageTexts(newForm.getStableId() + ":" + newForm.getVersion(), newForm.getPortalId(), parsedTitles);
        languageTextDao.bulkCreate(texts);

        return newForm;
    }

    @Transactional
    public ConsentForm createNewVersion(UUID portalId, ConsentForm consentForm) {
        ConsentForm newConsent = new ConsentForm();
        BeanUtils.copyProperties(consentForm, newConsent, "id", "version", "createdAt", "lastUpdatedAt", "publishedVersion");
        newConsent.setPortalId(portalId);
        int nextVersion = dao.getNextFormVersion(consentForm.getStableId(), portalId);
        newConsent.setVersion(nextVersion);
        ConsentForm newForm = create(newConsent);
        logger.info("Created new ConsentForm version:  stableId: {}, version: {}");
        return newForm;
    }

    public int getNextVersion(String stableId, UUID portalId) {
        return dao.getNextFormVersion(stableId, portalId);
    }
}
