package bio.terra.pearl.core.service.consent;

import bio.terra.pearl.core.dao.consent.ConsentResponseDao;
import bio.terra.pearl.core.model.consent.*;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.survey.ResponseData;
import bio.terra.pearl.core.model.workflow.HubResponse;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.model.workflow.TaskStatus;
import bio.terra.pearl.core.service.ImmutableEntityService;
import bio.terra.pearl.core.service.participant.ParticipantTaskService;
import bio.terra.pearl.core.service.study.StudyEnvironmentConsentService;
import bio.terra.pearl.core.service.workflow.EventService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConsentResponseService extends ImmutableEntityService<ConsentResponse, ConsentResponseDao> {
    private ConsentFormService consentFormService;
    private StudyEnvironmentConsentService studyEnvironmentConsentService;
    private ParticipantTaskService participantTaskService;
    private EventService eventService;
    private ObjectMapper objectMapper;

    public ConsentResponseService(ConsentResponseDao dao, ConsentFormService consentFormService,
                                  StudyEnvironmentConsentService studyEnvironmentConsentService,
                                  ParticipantTaskService participantTaskService,
                                  EventService eventService, ObjectMapper objectMapper) {
        super(dao);
        this.consentFormService = consentFormService;
        this.studyEnvironmentConsentService = studyEnvironmentConsentService;
        this.participantTaskService = participantTaskService;
        this.eventService = eventService;
        this.objectMapper = objectMapper;
    }

    public List<ConsentResponse> findByEnrolleeId(UUID enrolleeId) {
        return dao.findByEnrolleeId(enrolleeId);
    }


    public ConsentWithResponses findWithResponses(UUID studyEnvId, String stableId, Integer version,
                                                  Enrollee enrollee, UUID participantUserId) {
        ConsentForm form = consentFormService.findByStableId(stableId, version).get();
        // TODO we should only get the most recent response, and we should search by stableId, not form id, in
        // case they have a previous response to a different version of the form.
        List<ConsentResponse> responses = dao.findByEnrolleeId(enrollee.getId(), form.getId());
        // TODO this lookup should be by stableId -- it will fail if the version has been updated
        StudyEnvironmentConsent configConsent = studyEnvironmentConsentService
                .findByConsentForm(studyEnvId, form.getId()).get();
        configConsent.setConsentForm(form);
        return new ConsentWithResponses(
            configConsent, responses
        );
    }

    /**
     * Creates a consent response and fires appropriate downstream events.
     */
    @Transactional
    public HubResponse<ConsentResponse> submitResponse(UUID participantUserId, PortalParticipantUser ppUser,
                                                       Enrollee enrollee, UUID taskId, ConsentResponseDto responseDto) {
        ParticipantTask task = participantTaskService.authTaskToPortalParticipantUser(taskId, ppUser.getId()).get();
        ConsentForm responseForm = consentFormService.find(responseDto.getConsentFormId()).get();
        validateResponse(responseDto, responseForm, task);
        processResponseDto(responseDto);
        ConsentResponse response = create(participantUserId, enrollee.getId(), task, responseDto);

        // now update the task status and response id
        task.setStatus(response.isConsented() ? TaskStatus.COMPLETE : TaskStatus.REJECTED);
        task.setConsentResponseId(response.getId());
        participantTaskService.update(task);

        EnrolleeConsentEvent event = eventService.publishEnrolleeConsentEvent(enrollee, response, ppUser);
        logger.info("ConsentResponse submitted: enrollee: {}, formStableId: {}, formVersion: {}",
                enrollee.getShortcode(), responseForm.getVersion(), responseForm.getStableId() );
        HubResponse hubResponse = eventService.buildHubResponse(event, response);
        return hubResponse;
    }


    @Transactional
    public ConsentResponse create(UUID participantUserId, UUID enrolleeId, ParticipantTask task,
                                                     ConsentResponseDto responseDto) {
        ConsentResponse response = ConsentResponse.builder()
                .consentFormId(responseDto.getConsentFormId())
                .creatingParticipantUserId(participantUserId)
                .consented(responseDto.isConsented())
                .resumeData(responseDto.getResumeData())
                .fullData(responseDto.getFullData())
                .enrolleeId(enrolleeId)
                .build();
        return dao.create(response);
    }

    /** the frontend might pass either parsed or string data back, handle either case */
    public void processResponseDto(ConsentResponseDto response) {
        try {
            if (response.getFullData() == null && response.getParsedData() != null) {
                response.setFullData(objectMapper.writeValueAsString(response.getParsedData()));
            }
            if (response.getParsedData() == null && response.getFullData() != null) {
                response.setParsedData(objectMapper.readValue(response.getFullData(), ResponseData.class));
            }
        } catch (JsonProcessingException jpe) {
            throw new IllegalArgumentException("Could not process response:", jpe);
        }
    }

    public void validateResponse(ConsentResponseDto responseDto, ConsentForm form, ParticipantTask task) {
        if (!form.getStableId().equals(task.getTargetStableId())) {
            throw new IllegalArgumentException("submitted form does not match assigned task");
        }
        // TODO validate the response has the required fields from the form
    }

}
