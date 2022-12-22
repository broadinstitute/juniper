package bio.terra.pearl.core.dao.study;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.dao.consent.ConsentFormDao;
import bio.terra.pearl.core.model.consent.ConsentForm;
import bio.terra.pearl.core.model.consent.StudyEnvironmentConsent;
import java.util.List;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

@Component
public class StudyEnvironmentConsentDao extends BaseMutableJdbiDao<StudyEnvironmentConsent> {
    private ConsentFormDao consentFormDao;

    public StudyEnvironmentConsentDao(Jdbi jdbi, ConsentFormDao consentFormDao) {
        super(jdbi);
        this.consentFormDao = consentFormDao;
    }

    @Override
    protected Class<StudyEnvironmentConsent> getClazz() {
        return StudyEnvironmentConsent.class;
    }

    public List<StudyEnvironmentConsent> findAllByStudyEnvironmentId(UUID studyEnvId) {
        return findAllByProperty("study_environment_id", studyEnvId);
    }

    /** gets all the study environment surveys and attaches the relevant survey objects in a batch */
    public List<StudyEnvironmentConsent> findAllByStudyEnvIdWithConsent(UUID studyEnvId) {
        List<StudyEnvironmentConsent> studyEnvConsents = findAllByStudyEnvironmentId(studyEnvId);
        List<UUID> consentIds = studyEnvConsents.stream().map(ses -> ses.getConsentFormId()).toList();
        List<ConsentForm> consents = consentFormDao.findAllById(consentIds);
        for (StudyEnvironmentConsent sec : studyEnvConsents) {
            sec.setConsentForm(consents.stream().filter(survey -> survey.getId().equals(sec.getConsentFormId()))
                    .findFirst().get());
        }
        return studyEnvConsents;
    }

    public void deleteByStudyEnvironmentId(UUID studyEnvId) {
        deleteByUuidProperty("study_environment_id", studyEnvId);
    }
}
