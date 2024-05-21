package bio.terra.pearl.core.factory.survey;


import java.util.UUID;

import bio.terra.pearl.core.dao.survey.AnswerMappingDao;
import bio.terra.pearl.core.model.survey.AnswerMapping;
import bio.terra.pearl.core.model.survey.AnswerMappingMapType;
import bio.terra.pearl.core.model.survey.AnswerMappingTargetType;
import bio.terra.pearl.core.model.survey.PreEnrollmentResponse;
import bio.terra.pearl.core.service.survey.PreEnrollmentResponseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PreEnrollmentSurveyFactory {
    @Autowired
    PreEnrollmentResponseService preEnrollmentResponseService;
    @Autowired
    AnswerMappingDao answerMappingDao;

    public PreEnrollmentResponse buildPersisted(String testName, UUID surveyId, boolean qualified, String fullData, UUID portalParticipantId, UUID creatingUserId, UUID studyEnvironmentId) {
        PreEnrollmentResponse preEnrollmentResponse = PreEnrollmentResponse.builder()
                .fullData(fullData)
                .portalParticipantUserId(portalParticipantId)
                .qualified(qualified)
                        .creatingParticipantUserId(creatingUserId)
                        .surveyId(surveyId)
                        .studyEnvironmentId(studyEnvironmentId).build();
        return preEnrollmentResponseService.create(preEnrollmentResponse);
    }

    public void buildPersistedProxyAnswerMapping(String testName, UUID surveyId, String questionStableId) {
        AnswerMapping answerMapping = AnswerMapping.builder()
        .targetField("isProxy")
        .mapType(AnswerMappingMapType.STRING_TO_BOOLEAN)
                .targetType(AnswerMappingTargetType.PROXY)
                .questionStableId(questionStableId)
                .surveyId(surveyId).build();
        answerMappingDao.create(answerMapping);
    }
}
