package bio.terra.pearl.core.service.participant;

import bio.terra.pearl.core.dao.participant.FamilyEnrolleeDao;
import bio.terra.pearl.core.model.participant.FamilyEnrollee;
import bio.terra.pearl.core.service.DataAuditedService;
import bio.terra.pearl.core.service.workflow.DataChangeRecordService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class FamilyEnrolleeService extends DataAuditedService<FamilyEnrollee, FamilyEnrolleeDao> {

    public FamilyEnrolleeService(FamilyEnrolleeDao familyEnrolleeDao,
                                 DataChangeRecordService dataChangeRecordService,
                                 ObjectMapper objectMapper) {
        super(familyEnrolleeDao, dataChangeRecordService, objectMapper);
    }

    @Transactional
    public void deleteByFamilyId(UUID familyId) {
        dao.deleteByFamilyId(familyId);
    }

    @Transactional
    public void deleteByEnrolleeId(UUID enrolleeId) {
        dao.deleteByEnrolleeId(enrolleeId);
    }



}
