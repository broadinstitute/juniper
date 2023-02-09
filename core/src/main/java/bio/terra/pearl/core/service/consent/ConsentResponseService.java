package bio.terra.pearl.core.service.consent;

import bio.terra.pearl.core.dao.consent.ConsentResponseDao;
import bio.terra.pearl.core.model.consent.*;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.workflow.HubResponse;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.model.workflow.TaskStatus;
import bio.terra.pearl.core.service.CrudService;
import bio.terra.pearl.core.service.TransactionHandler;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.ParticipantTaskService;
import bio.terra.pearl.core.service.participant.PortalParticipantUserService;
import bio.terra.pearl.core.service.study.StudyEnvironmentConsentService;
import bio.terra.pearl.core.service.workflow.EnrolleeEventService;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConsentResponseService extends CrudService<ConsentResponse, ConsentResponseDao> {
    private static final Logger logger = LoggerFactory.getLogger(ConsentResponseService.class);
    private ConsentFormService consentFormService;
    private StudyEnvironmentConsentService studyEnvironmentConsentService;
    private EnrolleeService enrolleeService;
    private ParticipantTaskService participantTaskService;
    private PortalParticipantUserService portalParticipantUserService;
    private EnrolleeEventService enrolleeEventService;
    private TransactionHandler transactionHandler;

    public ConsentResponseService(ConsentResponseDao dao, ConsentFormService consentFormService,
                                  StudyEnvironmentConsentService studyEnvironmentConsentService,
                                  @Lazy EnrolleeService enrolleeService,
                                  ParticipantTaskService participantTaskService,
                                  PortalParticipantUserService portalParticipantUserService,
                                  EnrolleeEventService enrolleeEventService, TransactionHandler transactionHandler) {
        super(dao);
        this.consentFormService = consentFormService;
        this.studyEnvironmentConsentService = studyEnvironmentConsentService;
        this.enrolleeService = enrolleeService;
        this.participantTaskService = participantTaskService;
        this.portalParticipantUserService = portalParticipantUserService;
        this.enrolleeEventService = enrolleeEventService;
        this.transactionHandler = transactionHandler;
    }

    public List<ConsentResponse> findByEnrolleeId(UUID enrolleeId) {
        return dao.findByEnrolleeId(enrolleeId);
    }


    public ConsentWithResponses findWithResponses(UUID studyEnvId, String stableId, Integer version,
                                                  String enrolleeShortcode, UUID participantUserId) {
        Enrollee enrollee = enrolleeService.findOneByShortcode(enrolleeShortcode).get();
        enrolleeService.authParticipantUserToEnrollee(participantUserId, enrollee.getId());
        ConsentForm form = consentFormService.findByStableId(stableId, version).get();
        // TODO we should only get the most recent response, and we should search by stableId, not form id, in
        // case they have a previous response to a different version of the form.
        List<ConsentResponse> responses = dao.findByEnrolleeId(enrollee.getId(), form.getId());
        // TODO this lookup should be by stabelId -- it will fail if the version has been updated
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
                                                       String enrolleeShortcode, UUID taskId, ConsentResponseDto responseDto) {
        Enrollee enrollee = enrolleeService.authParticipantUserToEnrollee(participantUserId, enrolleeShortcode);
        ParticipantTask task = participantTaskService.authTaskToPortalParticipantUser(taskId, ppUser.getId()).get();

        ConsentResponse response = create(participantUserId, enrollee.getId(), responseDto);

        // now update the task status and response id
        task.setStatus(response.isConsented() ? TaskStatus.COMPLETE : TaskStatus.REJECTED);
        task.setConsentResponseId(response.getId());
        participantTaskService.update(task);

        EnrolleeConsentEvent event = enrolleeEventService.publishEnrolleeConsentEvent(enrollee, response, ppUser);
        HubResponse hubResponse = HubResponse.builder()
                .response(event.getConsentResponse())
                .tasks(event.getEnrollee().getParticipantTasks().stream().toList())
                .enrollee(event.getEnrollee()).build();
        return hubResponse;
    }


    @Transactional
    public ConsentResponse create(UUID participantUserId, UUID enrolleeId,
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

}
