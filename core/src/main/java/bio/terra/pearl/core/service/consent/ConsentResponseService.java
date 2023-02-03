package bio.terra.pearl.core.service.consent;

import bio.terra.pearl.core.dao.consent.ConsentResponseDao;
import bio.terra.pearl.core.dao.consent.ConsentWithResponses;
import bio.terra.pearl.core.model.consent.ConsentForm;
import bio.terra.pearl.core.model.consent.ConsentResponse;
import bio.terra.pearl.core.model.consent.StudyEnvironmentConsent;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.service.CrudService;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.study.StudyEnvironmentConsentService;
import java.util.List;
import java.util.UUID;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class ConsentResponseService extends CrudService<ConsentResponse, ConsentResponseDao> {
    private ConsentFormService consentFormService;
    private StudyEnvironmentConsentService studyEnvironmentConsentService;
    private EnrolleeService enrolleeService;

    public ConsentResponseService(ConsentResponseDao dao, ConsentFormService consentFormService,
                                  StudyEnvironmentConsentService studyEnvironmentConsentService,
                                  @Lazy EnrolleeService enrolleeService) {
        super(dao);
        this.consentFormService = consentFormService;
        this.studyEnvironmentConsentService = studyEnvironmentConsentService;
        this.enrolleeService = enrolleeService;
    }

    public List<ConsentResponse> findByEnrolleeId(UUID enrolleeId) {
        return dao.findByEnrolleeId(enrolleeId);
    }


    public ConsentWithResponses findWithResponses(UUID studyEnvId, String stableId, Integer version,
                                                  UUID participantUserId) {
        Enrollee enrollee = enrolleeService.findByParticipantUserId(participantUserId, studyEnvId).get();
        ConsentForm form = consentFormService.findByStableId(stableId, version).get();
        List<ConsentResponse> responses = dao.findByEnrolleeId(enrollee.getId(), form.getId());
        StudyEnvironmentConsent configConsent = studyEnvironmentConsentService
                .findByConsentForm(studyEnvId, form.getId()).get();
        configConsent.setConsentForm(form);
        return new ConsentWithResponses(
            configConsent, responses
        );
    }
}
