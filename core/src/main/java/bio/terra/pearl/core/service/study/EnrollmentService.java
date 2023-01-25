package bio.terra.pearl.core.service.study;

import bio.terra.pearl.core.dao.survey.PreEnrollmentResponseDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.survey.PreEnrollmentResponse;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.PortalParticipantUserService;
import bio.terra.pearl.core.service.survey.SurveyService;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EnrollmentService {
    private SurveyService surveyService;
    private PreEnrollmentResponseDao preEnrollmentResponseDao;
    private StudyEnvironmentService studyEnvironmentService;
    private PortalParticipantUserService portalParticipantUserService;
    private EnrolleeService enrolleeService;

    public EnrollmentService(SurveyService surveyService, PreEnrollmentResponseDao preEnrollmentResponseDao,
                             StudyEnvironmentService studyEnvironmentService,
                             PortalParticipantUserService portalParticipantUserService,
                             EnrolleeService enrolleeService) {
        this.surveyService = surveyService;
        this.preEnrollmentResponseDao = preEnrollmentResponseDao;
        this.studyEnvironmentService = studyEnvironmentService;
        this.portalParticipantUserService = portalParticipantUserService;
        this.enrolleeService = enrolleeService;
    }

    public Optional<PreEnrollmentResponse> findPreEnrollResponse(UUID responseId) {
        return preEnrollmentResponseDao.find(responseId);
    }

    /** Creates a preenrollment survey record for a user who is not signed in */
    @Transactional
    public PreEnrollmentResponse createAnonymousPreEnroll(
            UUID studyEnvironmentId,
            String surveyStableId,
            Integer surveyVersion,
            String fullData) {
        Survey survey = surveyService.findByStableId(surveyStableId, surveyVersion).get();
        PreEnrollmentResponse response = PreEnrollmentResponse.builder()
                        .surveyId(survey.getId())
                        .fullData(fullData)
                        .studyEnvironmentId(studyEnvironmentId).build();
        return preEnrollmentResponseDao.create(response);
    }

    @Transactional
    public Enrollee enroll(ParticipantUser user, String portalShortcode, EnvironmentName envName,
                                            String studyShortcode, UUID preEnrollResponseId) {

        StudyEnvironment studyEnv = studyEnvironmentService.findByStudy(studyShortcode, envName).get();
        PortalParticipantUser ppUser = portalParticipantUserService.findOne(user.getId(), portalShortcode).get();
        Enrollee enrollee = Enrollee.builder()
                .studyEnvironmentId(studyEnv.getId())
                .participantUserId(user.getId())
                .build();
        enrollee = enrolleeService.create(enrollee);
        if (preEnrollResponseId != null) {
            PreEnrollmentResponse response = preEnrollmentResponseDao.find(preEnrollResponseId).get();
            if (response.getCreatingParticipantUserId() != null &&
                    response.getCreatingParticipantUserId() != user.getId()) {
                throw new IllegalArgumentException("user does not match preEnrollment response user");
            }
            response.setEnrolleeId(enrollee.getId());
            response.setCreatingParticipantUserId(user.getId());
            response.setPortalParticipantUserId(ppUser.getId());
            preEnrollmentResponseDao.update(response);
        }
        return enrollee;
    }
}
