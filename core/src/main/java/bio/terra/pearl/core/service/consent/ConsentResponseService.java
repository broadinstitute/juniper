package bio.terra.pearl.core.service.consent;

import bio.terra.pearl.core.dao.consent.ConsentResponseDao;
import bio.terra.pearl.core.model.consent.*;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.workflow.HubResponse;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.model.workflow.TaskStatus;
import bio.terra.pearl.core.service.ImmutableEntityService;
import bio.terra.pearl.core.service.TransactionHandler;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.ParticipantTaskService;
import bio.terra.pearl.core.service.participant.PortalParticipantUserService;
import bio.terra.pearl.core.service.study.StudyEnvironmentConsentService;
import bio.terra.pearl.core.service.workflow.EventService;
import java.util.List;
import java.util.UUID;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConsentResponseService extends ImmutableEntityService<ConsentResponse, ConsentResponseDao> {
    private ConsentFormService consentFormService;
    private StudyEnvironmentConsentService studyEnvironmentConsentService;
    private EnrolleeService enrolleeService;
    private ParticipantTaskService participantTaskService;
    private PortalParticipantUserService portalParticipantUserService;
    private EventService eventService;
    private TransactionHandler transactionHandler;

    public ConsentResponseService(ConsentResponseDao dao, ConsentFormService consentFormService,
                                  StudyEnvironmentConsentService studyEnvironmentConsentService,
                                  @Lazy EnrolleeService enrolleeService,
                                  ParticipantTaskService participantTaskService,
                                  PortalParticipantUserService portalParticipantUserService,
                                  EventService eventService, TransactionHandler transactionHandler) {
        super(dao);
        this.consentFormService = consentFormService;
        this.studyEnvironmentConsentService = studyEnvironmentConsentService;
        this.enrolleeService = enrolleeService;
        this.participantTaskService = participantTaskService;
        this.portalParticipantUserService = portalParticipantUserService;
        this.eventService = eventService;
        this.transactionHandler = transactionHandler;
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

    public void validateResponse(ConsentResponseDto responseDto, ConsentForm form, ParticipantTask task) {
        if (!form.getStableId().equals(task.getTargetStableId())) {
            throw new IllegalArgumentException("submitted form does not match assigned task");
        }
        // TODO validate the response has the required fields from the form
    }

}
