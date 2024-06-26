package bio.terra.pearl.core.service.participant;

import bio.terra.pearl.core.dao.participant.EnrolleeRelationDao;
import bio.terra.pearl.core.dao.participant.FamilyDao;
import bio.terra.pearl.core.dao.participant.ProfileDao;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.participant.Family;
import bio.terra.pearl.core.service.DataAuditedService;
import bio.terra.pearl.core.service.workflow.DataChangeRecordService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public FamilyService(FamilyDao familyDao,
                         DataChangeRecordService dataChangeRecordService,
                         ObjectMapper objectMapper,
                         ShortcodeService shortcodeService,
                         @Lazy EnrolleeService enrolleeService,
                         EnrolleeRelationDao enrolleeRelationDao,
                         ProfileDao profileDao,
                         FamilyEnrolleeService familyEnrolleeService) {
        super(familyDao, dataChangeRecordService, objectMapper);
        this.shortcodeService = shortcodeService;
        this.enrolleeService = enrolleeService;
        this.enrolleeRelationDao = enrolleeRelationDao;
        this.profileDao = profileDao;
        this.familyEnrolleeService = familyEnrolleeService;
    }

    @Transactional
    public Family create(Family family, DataAuditInfo info) {
        if (family.getShortcode() == null) {
            family.setShortcode(shortcodeService.generateShortcode("F", dao::findOneByShortcode));
        }
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
        family.setRelations(enrolleeRelationDao.findRelationsForFamily(family.getId()));
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


    // WARNING: This method is not audited; it should only be used during study population/repopulation
    @Transactional
    public void deleteByStudyEnvironmentId(UUID studyEnvironmentId) {
        dao.deleteByStudyEnvironmentId(studyEnvironmentId);
    }
}
