package bio.terra.pearl.populate.service;

import bio.terra.pearl.core.dao.participant.FamilyDao;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.EnrolleeRelation;
import bio.terra.pearl.core.model.participant.Family;
import bio.terra.pearl.core.model.participant.RelationshipType;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.participant.EnrolleeRelationService;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.FamilyService;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.populate.dto.participant.FamilyPopDto;
import bio.terra.pearl.populate.service.contexts.PortalPopulateContext;
import bio.terra.pearl.populate.service.contexts.StudyPopulateContext;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class FamilyPopulator extends BasePopulator<Family, FamilyPopDto, StudyPopulateContext> {
    private final FamilyDao familyDao;
    private final FamilyService familyService;
    private final EnrolleeService enrolleeService;
    private final EnrolleeRelationService enrolleeRelationService;
    private final StudyEnvironmentService studyEnvironmentService;

    public FamilyPopulator(FamilyService familyService,
                           FamilyDao familyDao,
                           EnrolleeService enrolleeService, EnrolleeRelationService enrolleeRelationService, StudyEnvironmentService studyEnvironmentService) {
        this.familyService = familyService;
        this.familyDao = familyDao;
        this.enrolleeService = enrolleeService;
        this.enrolleeRelationService = enrolleeRelationService;
        this.studyEnvironmentService = studyEnvironmentService;
    }

    public Family populateForPortal(FamilyPopDto popDto, PortalPopulateContext context,
                                       boolean overwrite, Portal portal) throws IOException {

    }

    @Override
    protected Class<FamilyPopDto> getDtoClazz() {
        return FamilyPopDto.class;
    }

    @Override
    public Family createNew(FamilyPopDto popDto, StudyPopulateContext context, boolean overwrite) {
        StudyEnvironment attachedEnv = studyEnvironmentService
                .findByStudy(context.getStudyShortcode(), context.getEnvironmentName()).get();

        Enrollee proband = enrolleeService.findOneByShortcode(popDto.getProbandShortcode()).orElseThrow();
        DataAuditInfo auditInfo = DataAuditInfo.builder().systemProcess(
                DataAuditInfo.systemProcessName(getClass(), "populateFamily")).build();

        Family family = familyService.create(
                Family
                        .builder()
                        .shortcode(popDto.getShortcode())
                        .probandEnrolleeId(proband.getId())
                        .studyEnvironmentId(attachedEnv.getId())
                        .build(),
                auditInfo
        );

        List<Enrollee> members = enrolleeService.findAllByShortcodes(popDto.getMemberShortcodes());
        addToFamily(family, members);

        createRelations(family, popDto.getRelations());

        return family;
    }

    private void addToFamily(Family family, List<Enrollee> members) {
        members.forEach(member -> {
            member.setFamilyId(family.getId());
            enrolleeService.update(member);
        });
    }

    private void syncFamilyMembers(Family family, List<Enrollee> members) {
        List<Enrollee> existingMembers = enrolleeService.findAllByFamilyId(family.getId());

        existingMembers.forEach(existingMember -> {
            if (!members.contains(existingMember)) {
                existingMember.setFamilyId(null);
                enrolleeService.update(existingMember);
            }
        });

        members.forEach(member -> {
            if (!existingMembers.contains(member)) {
                member.setFamilyId(family.getId());
                enrolleeService.update(member);
            }
        });
    }

    private void createRelations(Family family, List<FamilyPopDto.FamilyRelationship> relations) {
        DataAuditInfo auditInfo = DataAuditInfo.builder().systemProcess(
                DataAuditInfo.systemProcessName(getClass(), "populateFamily")).build();
        relations.forEach(relation -> {
            Enrollee enrollee = enrolleeService.findOneByShortcode(relation.shortcode()).orElseThrow();
            Enrollee target = enrolleeService.findOneByShortcode(relation.targetShortcode()).orElseThrow();
            enrolleeRelationService
                    .create(
                            EnrolleeRelation
                                    .builder()
                                    .enrolleeId(enrollee.getId())
                                    .targetEnrolleeId(target.getId())
                                    .familyRelationship(relation.relationship())
                                    .relationshipType(RelationshipType.FAMILY)
                                    .build(),
                            auditInfo
                    );
        });
    }

    @Override
    public Family createPreserveExisting(Family existingObj, FamilyPopDto popDto, StudyPopulateContext context) {

        return adminUserService.update(popDto);
    }

    @Override
    public Family overwriteExisting(Family existingObj, FamilyPopDto popDto, StudyPopulateContext context) {
        adminUserService.delete(existingObj.getId(), CascadeProperty.EMPTY_SET);
        return createNew(popDto, context, true);
    }

    @Override
    public Optional<Family> findFromDto(FamilyPopDto popDto, StudyPopulateContext context ) {
        return adminUserService.findByUsername(popDto.getUsername());
    }
}
