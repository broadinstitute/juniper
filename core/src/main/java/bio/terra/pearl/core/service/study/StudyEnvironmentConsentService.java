package bio.terra.pearl.core.service.study;

import bio.terra.pearl.core.dao.study.StudyEnvironmentConsentDao;
import bio.terra.pearl.core.model.consent.StudyEnvironmentConsent;
import bio.terra.pearl.core.service.CrudService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class StudyEnvironmentConsentService extends CrudService<StudyEnvironmentConsent, StudyEnvironmentConsentDao> {
    public StudyEnvironmentConsentService(StudyEnvironmentConsentDao dao) {
        super(dao);
    }

    public List<StudyEnvironmentConsent> findAllByStudyEnvIdWithConsent(UUID studyEnvId) {
        return dao.findAllByStudyEnvIdWithConsent(studyEnvId);
    }

    public List<StudyEnvironmentConsent> findAllByStudyEnvironmentId(UUID studyEnvId) {
        return dao.findAllByStudyEnvironmentId(studyEnvId);
    }

    public Optional<StudyEnvironmentConsent> findByConsentForm(UUID studyEnvId, UUID consentFormId) {
        return dao.findByConsentForm(studyEnvId, consentFormId);
    }

    public Optional<StudyEnvironmentConsent> findByConsentForm(UUID studyEnvId, String stableId) {
        List<StudyEnvironmentConsent> configs = dao.findByConsentForm(studyEnvId, stableId);
        // we don't yet have robust support for having multiple consents with the same stableId configured for an
        // environment.  For now, just pick one
        return configs.stream().findFirst();
    }

    public List<StudyEnvironmentConsent> findAllByConsentForm(UUID consentFormId) {
        return dao.findAllByConsentForm(consentFormId);
    }
}
