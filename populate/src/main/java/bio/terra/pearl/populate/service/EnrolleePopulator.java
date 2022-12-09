package bio.terra.pearl.populate.service;

import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.survey.ResponseSnapshot;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.survey.SurveyResponse;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.ParticipantUserService;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.survey.SurveyResponseService;
import bio.terra.pearl.core.service.survey.SurveyService;
import bio.terra.pearl.populate.dto.EnrolleePopDto;
import bio.terra.pearl.populate.dto.survey.ResponseSnapshotPopDto;
import bio.terra.pearl.populate.dto.survey.SurveyResponsePopDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;

@Service
public class EnrolleePopulator extends Populator<Enrollee> {
    private EnrolleeService enrolleeService;
    private StudyEnvironmentService studyEnvironmentService;
    private ParticipantUserService participantUserService;
    private SurveyService surveyService;
    private SurveyResponseService surveyResponseService;

    public EnrolleePopulator(FilePopulateService filePopulateService,
                             ObjectMapper objectMapper,
                             EnrolleeService enrolleeService,
                             StudyEnvironmentService studyEnvironmentService,
                             ParticipantUserService participantUserService, SurveyService surveyService,
                             SurveyResponseService surveyResponseService) {
        this.surveyService = surveyService;
        this.surveyResponseService = surveyResponseService;
        this.objectMapper = objectMapper;
        this.filePopulateService = filePopulateService;
        this.enrolleeService = enrolleeService;
        this.studyEnvironmentService = studyEnvironmentService;
        this.participantUserService = participantUserService;
    }

    @Override
    public Enrollee populateFromString(String fileString, FilePopulateConfig config) throws IOException {
        EnrolleePopDto enrolleeDto = objectMapper.readValue(fileString, EnrolleePopDto.class);
        Optional<Enrollee> existingEnrollee = enrolleeService.findOneByShortcode(enrolleeDto.getShortcode());
        existingEnrollee.ifPresent(exEnrollee ->
                enrolleeService.delete(exEnrollee.getId(), new HashSet<>())
        );
        StudyEnvironment attachedEnv = studyEnvironmentService
                .findByStudy(config.getStudyShortcode(), config.getEnvironmentName()).get();
        enrolleeDto.setStudyEnvironmentId(attachedEnv.getId());
        ParticipantUser attachedUser = participantUserService
                .findOne(enrolleeDto.getLinkedUsername(), config.getEnvironmentName()).get();
        enrolleeDto.setParticipantUserId(attachedUser.getId());
        Enrollee enrollee = enrolleeService.create(enrolleeDto);
        for (SurveyResponsePopDto responsePopDto : enrolleeDto.getSurveyResponseDtos()) {
            populateResponse(enrollee, responsePopDto);
        }
        return enrollee;
    }

    private void populateResponse(Enrollee enrollee, SurveyResponsePopDto responsePopDto) {
        Survey survey = surveyService.findByStableId(responsePopDto.getSurveyStableId(),
                responsePopDto.getSurveyVersion()).get();

        SurveyResponse response = SurveyResponse.builder()
                .surveyId(survey.getId())
                .enrolleeId(enrollee.getId())
                .creatingParticipantUserId(enrollee.getParticipantUserId())
                .build();
        for (ResponseSnapshotPopDto snapDto : responsePopDto.getResponseSnapshotDtos()) {
            response.getSnapshots().add(
                    ResponseSnapshot.builder()
                            .fullData(snapDto.getFullDataJson().toString())
                            .resumeData(snapDto.getResumeDataJson().toString())
                            .creatingParticipantUserId(enrollee.getParticipantUserId())
                            .build()
            );
        }
        SurveyResponse savedResponse = surveyResponseService.create(response);
        enrollee.getSurveyResponses().add(savedResponse);
    }
}

