package bio.terra.pearl.core.service.consent;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.dao.consent.ConsentWithResponses;
import bio.terra.pearl.core.factory.consent.ConsentResponseFactory;
import bio.terra.pearl.core.model.consent.ConsentForm;
import bio.terra.pearl.core.model.consent.ConsentResponse;
import bio.terra.pearl.core.model.consent.StudyEnvironmentConsent;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.study.StudyEnvironmentConsentService;
import java.util.List;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ConsentResponseServiceTests extends BaseSpringBootTest {
    @Autowired
    private ConsentResponseFactory consentResponseFactory;
    @Autowired
    private ConsentResponseService consentResponseService;
    @Autowired
    private EnrolleeService enrolleeService;
    @Autowired
    private ConsentFormService consentFormService;
    @Autowired
    private StudyEnvironmentConsentService configConsentService;

    @Test
    public void testResponseCreate() {
        ConsentResponse consentResponse = consentResponseFactory.builderWithDependencies("testResponseCreate")
                .build();
        ConsentResponse savedResponse = consentResponseService.create(consentResponse);
        assertThat(savedResponse.getId(), notNullValue());
        assertThat(savedResponse.getCreatedAt(), notNullValue());
        assertThat(savedResponse.getFullData(), equalTo(consentResponse.getFullData()));

        List<ConsentResponse> responses = consentResponseService.findByEnrolleeId(consentResponse.getEnrolleeId());
        assertThat(responses, hasSize(1));
        assertThat(responses.get(0).getId(), equalTo(savedResponse.getId()));

        Enrollee enrollee = enrolleeService.find(savedResponse.getEnrolleeId()).get();
        ConsentForm consentForm = consentFormService.find(savedResponse.getConsentFormId()).get();
        configConsentService.create(StudyEnvironmentConsent.builder()
                .consentFormId(consentForm.getId())
                .studyEnvironmentId(enrollee.getStudyEnvironmentId())
                .consentOrder(4)
                .build());


        ConsentWithResponses consentWithResponses = consentResponseService
                .findWithResponses(enrollee.getStudyEnvironmentId(),
                        consentForm.getStableId(), consentForm.getVersion(), enrollee.getParticipantUserId());
        assertThat(consentWithResponses, notNullValue());
        assertThat(consentWithResponses.consentResponses(), hasSize(1));
        assertThat(consentWithResponses.studyEnvironmentConsent().getConsentOrder(), equalTo(4));
        assertThat(consentWithResponses.studyEnvironmentConsent().getConsentForm().getId(),
                equalTo(consentForm.getId()));
    }
}
