package bio.terra.pearl.core.dao.study;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.dao.consent.ConsentFormDao;
import bio.terra.pearl.core.model.consent.ConsentForm;
import bio.terra.pearl.core.model.consent.StudyEnvironmentConsent;
import java.util.List;
import java.util.Optional;
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

    /** gets all the study environment consents and attaches the relevant survey objects in a batch */
    public List<StudyEnvironmentConsent> findAllByStudyEnvIdWithConsent(UUID studyEnvId) {
        List<StudyEnvironmentConsent> studyEnvConsents = findAllByStudyEnvironmentId(studyEnvId);
        List<UUID> consentIds = studyEnvConsents.stream().map(ses -> ses.getConsentFormId()).toList();
        List<ConsentForm> consents = consentFormDao.findAll(consentIds);
        for (StudyEnvironmentConsent sec : studyEnvConsents) {
            sec.setConsentForm(consents.stream().filter(survey -> survey.getId().equals(sec.getConsentFormId()))
                    .findFirst().get());
        }
        return studyEnvConsents;
    }

    public List<StudyEnvironmentConsent> findByConsentForm(UUID studyEnvId, String consentStableId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("select " + prefixedGetQueryColumns("a") + " from " + tableName +
                        " a join consent_form on consent_form.id = a.consent_form_id " +
                        " where consent_form.stable_id = :stableId " +
                        " and a.study_environment_id = :studyEnvId;")
                        .bind("stableId", consentStableId)
                        .bind("studyEnvId", studyEnvId)
                        .mapTo(clazz)
                        .list()
        );
    }

    public Optional<StudyEnvironmentConsent> findByConsentForm(UUID studyEnvId, UUID consentFormId) {
        return findByTwoProperties("study_environment_id",studyEnvId,
                "consent_form_id", consentFormId);
    }

    public void deleteByStudyEnvironmentId(UUID studyEnvId) {
        deleteByProperty("study_environment_id", studyEnvId);
    }
}
