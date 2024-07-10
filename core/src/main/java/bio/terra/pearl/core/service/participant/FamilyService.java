package bio.terra.pearl.core.service.participant;

import bio.terra.pearl.core.dao.participant.EnrolleeRelationDao;
import bio.terra.pearl.core.dao.participant.FamilyDao;
import bio.terra.pearl.core.dao.participant.ProfileDao;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.audit.DataChangeRecord;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.Family;
import bio.terra.pearl.core.model.participant.FamilyEnrollee;
import bio.terra.pearl.core.service.DataAuditedService;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.workflow.DataChangeRecordService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jooq.tools.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class FamilyService extends DataAuditedService<Family, FamilyDao> {
    private final ShortcodeService shortcodeService;
    private final EnrolleeService enrolleeService;
    private final EnrolleeRelationDao enrolleeRelationDao;
    private final ProfileDao profileDao;
    private final FamilyEnrolleeService familyEnrolleeService;
    private final EnrolleeRelationService enrolleeRelationService;

    public FamilyService(FamilyDao familyDao,
                         DataChangeRecordService dataChangeRecordService,
                         ObjectMapper objectMapper,
                         ShortcodeService shortcodeService,
                         @Lazy EnrolleeService enrolleeService,
                         EnrolleeRelationDao enrolleeRelationDao,
                         ProfileDao profileDao,
                         FamilyEnrolleeService familyEnrolleeService, EnrolleeRelationService enrolleeRelationService) {
        super(familyDao, dataChangeRecordService, objectMapper);
        this.shortcodeService = shortcodeService;
        this.enrolleeService = enrolleeService;
        this.enrolleeRelationDao = enrolleeRelationDao;
        this.profileDao = profileDao;
        this.familyEnrolleeService = familyEnrolleeService;
        this.enrolleeRelationService = enrolleeRelationService;
    }

    @Transactional
    public Family create(Family family, DataAuditInfo info) {
        if (StringUtils.isBlank(family.getShortcode())) {
            family.setShortcode(shortcodeService.generateShortcode("F", dao::findOneByShortcode));
        }
        family.setCreatedAt(Instant.now());
        family.setLastUpdatedAt(Instant.now());
        return super.create(family, info);
    }

    public Optional<Family> findOneByShortcode(String shortcode) {
        return dao.findOneByShortcode(shortcode);
    }

    public Optional<Family> findOneByShortcodeAndStudyEnvironmentId(String shortcode, UUID studyEnvironmentId) {
        return dao.findOneByShortcodeAndStudyEnvironmentId(shortcode, studyEnvironmentId);
    }

    public Family loadForAdminView(Family family) {
        family.setMembers(enrolleeService.findAllByFamilyId(family.getId()));
        enrolleeService.attachProfiles(family.getMembers());
        family.setRelations(enrolleeRelationService.findRelationsForFamily(family.getId()).stream().map(relation -> {
            enrolleeRelationService.attachEnrolleesAndFamily(relation);
            return relation;
        }).toList());
        family.setProband(enrolleeService.find(family.getProbandEnrolleeId()).map(enrolleeService::attachProfile).orElse(null));
        return family;
    }

    public List<Family> findByStudyEnvironmentId(UUID studyEnvironmentId) {
        return dao.findByStudyEnvironmentId(studyEnvironmentId);
    }

    @Override
    @Transactional
    public void delete(UUID familyId, DataAuditInfo info) {
        familyEnrolleeService.deleteByFamilyId(familyId, info);

        super.delete(familyId, info);
    }


    public List<Family> findByEnrolleeId(UUID enrolleeId) {
        return dao.findByEnrolleeId(enrolleeId);
    }

    public List<Family> findByEnrolleeIdWithProband(UUID enrolleeId) {
        List<Family> families = dao.findByEnrolleeId(enrolleeId);
        families.forEach(family -> {
            if (family.getProbandEnrolleeId() != null) {
                enrolleeService.find(family.getProbandEnrolleeId()).ifPresent(family::setProband);
            }
        });
        return families;
    }

    public boolean isEnrolleeInFamily(Family family, Enrollee enrollee) {
        List<FamilyEnrollee> existingMembers = familyEnrolleeService.findByFamilyId(family.getId());
        return existingMembers.stream().anyMatch(fe -> fe.getEnrolleeId().equals(enrollee.getId()));
    }

    // WARNING: This method is not audited; it should only be used during study population/repopulation
    @Transactional
    public void deleteByStudyEnvironmentId(UUID studyEnvironmentId) {
        dao.deleteByStudyEnvironmentId(studyEnvironmentId);
    }

    @Transactional
    public void addEnrollee(String familyShortcode,
                            String enrolleeShortcode,
                            UUID studyEnvId,
                            DataAuditInfo auditInfo) {
        // ensure the family and enrollee are in the expected study environment
        Family family = findOneByShortcodeAndStudyEnvironmentId(familyShortcode, studyEnvId)
                .orElseThrow(() -> new NotFoundException("Family not found"));

        Enrollee enrollee = enrolleeService.findByShortcodeAndStudyEnvId(enrolleeShortcode, studyEnvId)
                .orElseThrow(() -> new NotFoundException("Enrollee not found"));

        // ensure the enrollee is not already a member of the family
        if (isEnrolleeInFamily(family, enrollee)) {
            throw new IllegalArgumentException("Enrollee already in family");
        }

        // attach audit info to the enrollee (so it shows up in data audit table)
        auditInfo.setEnrolleeId(enrollee.getId());
        auditInfo.setFamilyId(family.getId());
        familyEnrolleeService.create(
                FamilyEnrollee.builder()
                        .familyId(family.getId())
                        .enrolleeId(enrollee.getId())
                        .build(),
                auditInfo
        );
    }

    @Transactional
    public void removeEnrollee(String familyShortcode,
                               String enrolleeShortcode,
                               UUID studyEnvId,
                               DataAuditInfo auditInfo) {
        Family family = findOneByShortcodeAndStudyEnvironmentId(familyShortcode, studyEnvId)
                .orElseThrow(() -> new NotFoundException("Family not found"));

        Enrollee enrollee = enrolleeService.findByShortcodeAndStudyEnvId(enrolleeShortcode, studyEnvId)
                .orElseThrow(() -> new NotFoundException("Enrollee not found"));

        if (!isEnrolleeInFamily(family, enrollee)) {
            throw new NotFoundException("Enrollee not found in in family");
        }

        if (family.getProbandEnrolleeId() != null && family.getProbandEnrolleeId().equals(enrollee.getId())) {
            throw new IllegalArgumentException("Cannot remove proband from family");
        }

        auditInfo.setFamilyId(family.getId());
        auditInfo.setEnrolleeId(enrollee.getId());
        // also cleans up any relationships the enrollee has within the family
        familyEnrolleeService.deleteFamilyEnrolleeAndAllRelationships(
                enrollee.getId(),
                family.getId(),
                auditInfo);
    }

    @Transactional
    public Family updateProband(String familyShortcode, String enrolleeShortcode, UUID studyEnvId, DataAuditInfo auditInfo) {
        Family family = findOneByShortcodeAndStudyEnvironmentId(familyShortcode, studyEnvId)
                .orElseThrow(() -> new NotFoundException("Family not found"));

        Enrollee enrollee = enrolleeService.findByShortcodeAndStudyEnvId(enrolleeShortcode, studyEnvId)
                .orElseThrow(() -> new NotFoundException("Enrollee not found"));

        familyEnrolleeService.findByFamilyIdAndEnrolleeId(family.getId(), enrollee.getId())
                .orElseThrow(() -> new IllegalArgumentException("Enrollee not in family"));

        family.setProbandEnrolleeId(enrollee.getId());
        auditInfo.setFamilyId(family.getId());
        auditInfo.setEnrolleeId(enrollee.getId());
        return update(family, auditInfo);
    }

    public List<DataChangeRecord> findDataChangeRecordsByFamilyId(UUID familyId) {
        return dataChangeRecordService.findByFamilyId(familyId);
    }

    public List<DataChangeRecord> findDataChangeRecordsByFamilyIdAndModelName(UUID familyId, String model) {
        return dataChangeRecordService.findByFamilyIdAndModelName(familyId, model);
    }

    @Override
    protected DataChangeRecord makeCreationChangeRecord(Family model, DataAuditInfo auditInfo) {
        DataChangeRecord changeRecord = super.makeCreationChangeRecord(model, auditInfo);
        changeRecord.setFamilyId(model.getId());
        return changeRecord;
    }
}
