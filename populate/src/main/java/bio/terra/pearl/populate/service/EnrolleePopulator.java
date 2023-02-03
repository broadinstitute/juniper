package bio.terra.pearl.populate.service;

import bio.terra.pearl.core.model.consent.ConsentForm;
import bio.terra.pearl.core.model.consent.ConsentResponse;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.survey.ResponseSnapshot;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.survey.SurveyResponse;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.consent.ConsentFormService;
import bio.terra.pearl.core.service.consent.ConsentResponseService;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.ParticipantTaskService;
import bio.terra.pearl.core.service.participant.ParticipantUserService;
import bio.terra.pearl.core.service.participant.PortalParticipantUserService;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.survey.SurveyResponseService;
import bio.terra.pearl.core.service.survey.SurveyService;
import bio.terra.pearl.populate.dto.EnrolleePopDto;
import bio.terra.pearl.populate.dto.ParticipantTaskPopDto;
import bio.terra.pearl.populate.dto.consent.ConsentResponsePopDto;
import bio.terra.pearl.populate.dto.survey.ResponseSnapshotPopDto;
import bio.terra.pearl.populate.dto.survey.SurveyResponsePopDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class EnrolleePopulator extends Populator<Enrollee> {
    private EnrolleeService enrolleeService;
    private StudyEnvironmentService studyEnvironmentService;
    private ParticipantUserService participantUserService;
    private PortalParticipantUserService portalParticipantUserService;
    private SurveyService surveyService;
    private SurveyResponseService surveyResponseService;
    private ConsentFormService consentFormService;
    private ConsentResponseService consentResponseService;
    private ParticipantTaskService participantTaskService;

    public EnrolleePopulator(FilePopulateService filePopulateService,
                             ObjectMapper objectMapper,
                             EnrolleeService enrolleeService,
                             StudyEnvironmentService studyEnvironmentService,
                             ParticipantUserService participantUserService,
                             PortalParticipantUserService portalParticipantUserService, SurveyService surveyService,
                             SurveyResponseService surveyResponseService, ConsentFormService consentFormService,
                             ConsentResponseService consentResponseService,
                             ParticipantTaskService participantTaskService) {
        this.portalParticipantUserService = portalParticipantUserService;
        this.surveyService = surveyService;
        this.surveyResponseService = surveyResponseService;
        this.consentFormService = consentFormService;
        this.consentResponseService = consentResponseService;
        this.participantTaskService = participantTaskService;
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
                enrolleeService.delete(exEnrollee.getId(), CascadeProperty.EMPTY_SET)
        );
        StudyEnvironment attachedEnv = studyEnvironmentService
                .findByStudy(config.getStudyShortcode(), config.getEnvironmentName()).get();
        enrolleeDto.setStudyEnvironmentId(attachedEnv.getId());
        ParticipantUser attachedUser = participantUserService
                .findOne(enrolleeDto.getLinkedUsername(), config.getEnvironmentName()).get();
        PortalParticipantUser ppUser = portalParticipantUserService
                .findOne(attachedUser.getId(), config.getPortalShortcode()).get();
        enrolleeDto.setParticipantUserId(attachedUser.getId());
        enrolleeDto.setProfileId(ppUser.getProfileId());
        Enrollee enrollee = enrolleeService.create(enrolleeDto);
        for (SurveyResponsePopDto responsePopDto : enrolleeDto.getSurveyResponseDtos()) {
            populateResponse(enrollee, responsePopDto);
        }
        for (ConsentResponsePopDto consentPopDto : enrolleeDto.getConsentResponseDtos()) {
            populateConsent(enrollee, consentPopDto);
        }
        for (ParticipantTaskPopDto taskDto : enrolleeDto.getParticipantTaskDtos()) {
            populateTask(enrollee, ppUser, taskDto);
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

    private void populateConsent(Enrollee enrollee, ConsentResponsePopDto responsePopDto) {
        ConsentForm consentForm = consentFormService.findByStableId(responsePopDto.getConsentStableId(),
                responsePopDto.getConsentVersion()).get();

        ConsentResponse response = ConsentResponse.builder()
                .consentFormId(consentForm.getId())
                .enrolleeId(enrollee.getId())
                .creatingParticipantUserId(enrollee.getParticipantUserId())
                .consented(responsePopDto.isConsented())
                .fullData(responsePopDto.getFullDataJson().toString())
                .resumeData(responsePopDto.getResumeDataJson().toString())
                .build();

        ConsentResponse savedResponse = consentResponseService.create(response);
        enrollee.getConsentResponses().add(savedResponse);
    }

    private void populateTask(Enrollee enrollee, PortalParticipantUser ppUser, ParticipantTaskPopDto taskDto) {
        taskDto.setEnrolleeId(enrollee.getId());
        taskDto.setStudyEnvironmentId(enrollee.getStudyEnvironmentId());
        taskDto.setPortalParticipantUserId(ppUser.getId());
        participantTaskService.create(taskDto);
    }
}

