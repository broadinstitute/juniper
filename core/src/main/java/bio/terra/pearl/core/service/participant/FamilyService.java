package bio.terra.pearl.core.service.participant;

import bio.terra.pearl.core.dao.participant.EnrolleeDao;
import bio.terra.pearl.core.dao.participant.FamilyDao;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.participant.Family;
import bio.terra.pearl.core.service.DataAuditedService;
import bio.terra.pearl.core.service.workflow.DataChangeRecordService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class FamilyService extends DataAuditedService<Family, FamilyDao> {
    private final ShortcodeService shortcodeService;
    private final FamilyEnrolleeService familyEnrolleeService;
    private final EnrolleeDao enrolleeDao;

    public FamilyService(FamilyDao familyDao,
                         DataChangeRecordService dataChangeRecordService,
                         ObjectMapper objectMapper,
                         ShortcodeService shortcodeService,
                         FamilyEnrolleeService familyEnrolleeService, EnrolleeDao enrolleeDao) {
        super(familyDao, dataChangeRecordService, objectMapper);
        this.shortcodeService = shortcodeService;
        this.familyEnrolleeService = familyEnrolleeService;
        this.enrolleeDao = enrolleeDao;
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
                enrolleeDao.find(family.getProbandEnrolleeId()).ifPresent(family::setProband);
            }
        });
        return families;
    }

    // WARNING: This method is not audited; it should only be used during study population/repopulation
    @Transactional
    public void deleteByStudyEnvironmentId(UUID studyEnvironmentId) {
        dao.deleteByStudyEnvironmentId(studyEnvironmentId);
    }
}
