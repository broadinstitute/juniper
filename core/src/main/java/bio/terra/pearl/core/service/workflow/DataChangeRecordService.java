package bio.terra.pearl.core.service.workflow;

import bio.terra.pearl.core.dao.workflow.DataChangeRecordDao;
import bio.terra.pearl.core.model.audit.DataChangeRecord;
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
public class DataChangeRecordService extends ImmutableEntityService<DataChangeRecord, DataChangeRecordDao> {
    private EnrolleeService enrolleeService;
    private PortalParticipantUserService portalParticipantUserService;


    public DataChangeRecordService(
            DataChangeRecordDao dao,
            @Lazy EnrolleeService enrolleeService,
            @Lazy PortalParticipantUserService portalParticipantUserService) {
        super(dao);
        this.enrolleeService = enrolleeService;
        this.portalParticipantUserService = portalParticipantUserService;
    }

    // There can be records which exist for an enrollee which, for one reason or another,
    // are tied only to the PortalParticipantUser. This grabs all of them
    // to ensure that we are missing nothing for a given enrollee.
    public List<DataChangeRecord> findAllRecordsForEnrollee(Enrollee enrollee) {
        PortalParticipantUser ppUser = portalParticipantUserService
                .findForEnrollee(enrollee);
        return dao.findAllRecordsForEnrollee(enrollee.getId(), ppUser.getId());
    }

    // There can be records which exist for an enrollee which, for one reason or another,
    // are tied only to the PortalParticipantUser. This grabs all of them
    // to ensure that we are missing nothing for a given enrollee.
    public List<DataChangeRecord> findAllRecordsForEnrolleeAndModelName(Enrollee enrollee, String modelName) {
        PortalParticipantUser ppUser = portalParticipantUserService
                .findForEnrollee(enrollee);
        return dao.findAllRecordsForEnrolleeAndModelName(enrollee.getId(), ppUser.getId(), modelName);
    }

    public List<DataChangeRecord> findByEnrollee(UUID enrolleeId) {
        return dao.findByEnrolleeId(enrolleeId);
    }

    public List<DataChangeRecord> findByPortalEnvironmentId(UUID portalEnvId) {
        return dao.findByPortalEnvironmentId(portalEnvId);
    }

    public List<DataChangeRecord> findByModelId(UUID modelId) { return dao.findByModelId(modelId); }

    @Transactional
    public void deleteByPortalParticipantUserId(UUID ppUserId) {
        dao.deleteByPortalParticipantUserId(ppUserId);
    }

    @Transactional
    public void deleteByResponsibleUserId(UUID responsibleUserId) {
        dao.deleteByResponsibleUserId(responsibleUserId);
    }

    @Transactional
    public void deleteByPortalEnvironmentId(UUID portalEnvId) {
        dao.deleteByPortalEnvironmentId(portalEnvId);
    }
    @Transactional
    public void deleteByEnrolleeId(UUID enrolleeId) {
        dao.deleteByEnrolleeId(enrolleeId);
    }

    public List<DataChangeRecord> findByFamilyId(UUID familyId) {
        return dao.findByFamilyId(familyId);
    }

    public List<DataChangeRecord> findByFamilyIdAndModelName(UUID familyId, String model) {
        return dao.findByFamilyIdAndModelName(familyId, model);
    }

    public void deleteByStudyEnvironmentId(UUID studyEnvironmentId) {
        dao.deleteByStudyEnvironmentId(studyEnvironmentId);
    }
}
