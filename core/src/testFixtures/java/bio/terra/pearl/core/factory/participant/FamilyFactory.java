package bio.terra.pearl.core.factory.participant;

import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.Family;
import bio.terra.pearl.core.model.participant.FamilyEnrollee;
import bio.terra.pearl.core.service.participant.FamilyEnrolleeService;
import bio.terra.pearl.core.service.participant.FamilyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FamilyFactory {
    @Autowired
    private FamilyService familyService;

    @Autowired
    private FamilyEnrolleeService familyEnrolleeService;


    public Family buildPersisted(String testName, Enrollee proband) {
        Family family = Family.builder().probandEnrolleeId(proband.getId()).studyEnvironmentId(proband.getStudyEnvironmentId()).build();

        DataAuditInfo auditInfo = DataAuditInfo.builder().systemProcess(
                DataAuditInfo.systemProcessName(getClass(), "buildPersisted")
        ).build();

        family = familyService.create(family, auditInfo);
        linkEnrolleeToFamily(proband, family);
        return family;
    }

    public FamilyEnrollee linkEnrolleeToFamily(Enrollee enrollee, Family family) {
        DataAuditInfo auditInfo = DataAuditInfo.builder().systemProcess(
                DataAuditInfo.systemProcessName(getClass(), "linkEnrolleeToFamily")
        ).build();

        return familyEnrolleeService.create(
                FamilyEnrollee
                        .builder()
                        .familyId(family.getId())
                        .enrolleeId(enrollee.getId())
                        .build(),
                auditInfo
        );
    }

}
