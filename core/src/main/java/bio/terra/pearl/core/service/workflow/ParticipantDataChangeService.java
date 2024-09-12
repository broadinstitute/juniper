package bio.terra.pearl.core.service.workflow;

import bio.terra.pearl.core.dao.workflow.ParticipantDataChangeDao;
import bio.terra.pearl.core.model.audit.ParticipantDataChange;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.service.ImmutableEntityService;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.PortalParticipantUserService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ParticipantDataChangeService extends ImmutableEntityService<ParticipantDataChange, ParticipantDataChangeDao> {
    private EnrolleeService enrolleeService;
    private PortalParticipantUserService portalParticipantUserService;


    public ParticipantDataChangeService(
            ParticipantDataChangeDao dao,
            @Lazy EnrolleeService enrolleeService,
            @Lazy PortalParticipantUserService portalParticipantUserService) {
        super(dao);
        this.enrolleeService = enrolleeService;
        this.portalParticipantUserService = portalParticipantUserService;
    }

    // There can be records which exist for an enrollee which, for one reason or another,
    // are tied only to the PortalParticipantUser. This grabs all of them
    // to ensure that we are missing nothing for a given enrollee.
    public List<ParticipantDataChange> findAllRecordsForEnrollee(Enrollee enrollee) {
        PortalParticipantUser ppUser = portalParticipantUserService
                .findForEnrollee(enrollee);
        return dao.findAllRecordsForEnrollee(enrollee.getId(), ppUser.getId());
    }

    // There can be records which exist for an enrollee which, for one reason or another,
    // are tied only to the PortalParticipantUser. This grabs all of them
    // to ensure that we are missing nothing for a given enrollee.
    public List<ParticipantDataChange> findAllRecordsForEnrolleeAndModelName(Enrollee enrollee, String modelName) {
        PortalParticipantUser ppUser = portalParticipantUserService
                .findForEnrollee(enrollee);
        return dao.findAllRecordsForEnrolleeAndModelName(enrollee.getId(), ppUser.getId(), modelName);
    }

    public List<ParticipantDataChange> findByEnrollee(UUID enrolleeId) {
        return dao.findByEnrolleeId(enrolleeId);
    }

    public List<ParticipantDataChange> findByPortalEnvironmentId(UUID portalEnvId) {
        return dao.findByPortalEnvironmentId(portalEnvId);
    }

    public List<ParticipantDataChange> findByModelId(UUID modelId) { return dao.findByModelId(modelId); }

    @Transactional
    public void deleteByPortalParticipantUserId(UUID ppUserId) {
        dao.deleteByPortalParticipantUserId(ppUserId);
    }

    @Transactional
    public void deleteByResponsibleUserId(UUID responsibleUserId) {
        dao.deleteByResponsibleUserId(responsibleUserId);
    }

    @Transactional
    public void deleteByResponsibleAdminUserId(UUID responsibleAdminUserId) {
        dao.deleteByResponsibleAdminUserId(responsibleAdminUserId);
    }

    @Transactional
    public void deleteByPortalEnvironmentId(UUID portalEnvId) {
        dao.deleteByPortalEnvironmentId(portalEnvId);
    }
    @Transactional
    public void deleteByEnrolleeId(UUID enrolleeId) {
        dao.deleteByEnrolleeId(enrolleeId);
    }

    public List<ParticipantDataChange> findByFamilyId(UUID familyId) {
        return dao.findByFamilyId(familyId);
    }

    public List<ParticipantDataChange> findByFamilyIdAndModelName(UUID familyId, String model) {
        return dao.findByFamilyIdAndModelName(familyId, model);
    }

    public void deleteByStudyEnvironmentId(UUID studyEnvironmentId) {
        dao.deleteByStudyEnvironmentId(studyEnvironmentId);
    }
}
