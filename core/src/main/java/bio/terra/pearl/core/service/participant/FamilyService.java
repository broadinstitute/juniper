package bio.terra.pearl.core.service.participant;

import bio.terra.pearl.core.dao.participant.FamilyDao;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.participant.Family;
import bio.terra.pearl.core.service.DataAuditedService;
import bio.terra.pearl.core.service.workflow.DataChangeRecordService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class FamilyService extends DataAuditedService<Family, FamilyDao> {
    private final ShortcodeService shortcodeService;

    public FamilyService(FamilyDao familyDao,
                         DataChangeRecordService dataChangeRecordService,
                         ObjectMapper objectMapper,
                         ShortcodeService shortcodeService) {
        super(familyDao, dataChangeRecordService, objectMapper);
        this.shortcodeService = shortcodeService;
    }

    @Transactional
    public Family create(Family family, DataAuditInfo info) {
        if (family.getShortcode() == null) {
            family.setShortcode(shortcodeService.generateShortcode("F", dao::findOneByShortcode));
        }
        return super.create(family, info);
    }


    public List<Family> findByEnrolleeId(UUID enrolleeId) {
        return dao.findByEnrolleeId(enrolleeId);
    }
}
