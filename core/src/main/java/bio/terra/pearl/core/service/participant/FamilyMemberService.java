package bio.terra.pearl.core.service.participant;

import bio.terra.pearl.core.dao.participant.FamilyMemberDao;
import bio.terra.pearl.core.model.participant.FamilyMember;
import bio.terra.pearl.core.service.DataAuditedService;
import bio.terra.pearl.core.service.workflow.DataChangeRecordService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class FamilyMemberService extends DataAuditedService<FamilyMember, FamilyMemberDao> {

    public FamilyMemberService(FamilyMemberDao familyMemberDao,
                               DataChangeRecordService dataChangeRecordService,
                               ObjectMapper objectMapper) {
        super(familyMemberDao, dataChangeRecordService, objectMapper);
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
