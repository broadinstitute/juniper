package bio.terra.pearl.core.service.consent;

import bio.terra.pearl.core.dao.consent.ConsentResponseDao;
import bio.terra.pearl.core.model.consent.*;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.workflow.HubResponse;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.model.workflow.TaskStatus;
import bio.terra.pearl.core.service.ImmutableEntityService;
import bio.terra.pearl.core.service.workflow.ParticipantTaskService;
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
        // this searches by form id -- we don't carry forward responses from one version of a consent to the next
        List<ConsentResponse> responses = dao.findByEnrolleeId(enrollee.getId(), form.getId());

        StudyEnvironmentConsent configConsent = studyEnvironmentConsentService
                .findByConsentForm(studyEnvId, stableId).get();
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
                                                       Enrollee enrollee, ConsentResponseDto responseDto) {
        ConsentForm responseForm = consentFormService.find(responseDto.getConsentFormId()).get();
        ParticipantTask task = participantTaskService
                .findTaskForActivity(ppUser.getId(), enrollee.getStudyEnvironmentId(), responseForm.getStableId()).get();
        validateResponse(responseDto, responseForm, task);
        processResponseDto(responseDto);
        ConsentResponse response = create(participantUserId, enrollee.getId(), task, responseDto);

        // now update the task status and response id
        task.setStatus(response.isConsented() ? TaskStatus.COMPLETE : TaskStatus.REJECTED);
        task.setConsentResponseId(response.getId());
        DataAuditInfo auditInfo = DataAuditInfo.builder()
                .responsibleUserId(participantUserId)
                .portalParticipantUserId(ppUser.getId())
                .enrolleeId(enrollee.getId()).build();
        participantTaskService.update(task, auditInfo);

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
            if (response.getFullData() == null && response.getAnswers() != null) {
                response.setFullData(objectMapper.writeValueAsString(response.getAnswers()));
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
