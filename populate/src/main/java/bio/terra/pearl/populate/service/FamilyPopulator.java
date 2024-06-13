package bio.terra.pearl.populate.service;

import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.participant.*;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.participant.EnrolleeRelationService;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.FamilyEnrolleeService;
import bio.terra.pearl.core.service.participant.FamilyService;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.populate.dto.participant.FamilyPopDto;
import bio.terra.pearl.populate.service.contexts.StudyPopulateContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class FamilyPopulator extends BasePopulator<Family, FamilyPopDto, StudyPopulateContext> {
    private final FamilyService familyService;
    private final EnrolleeService enrolleeService;
    private final EnrolleeRelationService enrolleeRelationService;
    private final StudyEnvironmentService studyEnvironmentService;
    private final FamilyEnrolleeService familyEnrolleeService;

    public FamilyPopulator(FamilyService familyService,
                           EnrolleeService enrolleeService,
                           EnrolleeRelationService enrolleeRelationService,
                           StudyEnvironmentService studyEnvironmentService, FamilyEnrolleeService familyEnrolleeService) {
        this.familyService = familyService;
        this.enrolleeService = enrolleeService;
        this.enrolleeRelationService = enrolleeRelationService;
        this.studyEnvironmentService = studyEnvironmentService;
        this.familyEnrolleeService = familyEnrolleeService;
    }

    @Override
    protected Class<FamilyPopDto> getDtoClazz() {
        return FamilyPopDto.class;
    }

    @Override
    @Transactional
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
            familyEnrolleeService.create(
                    FamilyEnrollee.builder().familyId(family.getId()).enrolleeId(member.getId()).build(),
                    DataAuditInfo.builder().systemProcess(
                            DataAuditInfo.systemProcessName(getClass(), "populateFamily")).build());
        });
    }

    private void syncFamilyMembers(Family family, List<Enrollee> members) {
        familyEnrolleeService.deleteByFamilyId(family.getId(),
                DataAuditInfo.builder().systemProcess(
                        DataAuditInfo.systemProcessName(getClass(), "populateFamily")).build());
        addToFamily(family, members);
    }

    private void createRelations(Family family, List<FamilyPopDto.FamilyRelationship> relations) {
        DataAuditInfo auditInfo = DataAuditInfo.builder().systemProcess(
                DataAuditInfo.systemProcessName(getClass(), "populateFamily")).build();

        relations.forEach(relation -> {
            Enrollee enrollee = enrolleeService.findOneByShortcode(relation.shortcode()).orElseThrow();
            Enrollee targetEnrollee = enrolleeService.findOneByShortcode(relation.targetShortcode()).orElseThrow();
            enrolleeRelationService.create(
                    EnrolleeRelation.builder()
                            .enrolleeId(enrollee.getId())
                            .targetEnrolleeId(targetEnrollee.getId())
                            .familyId(family.getId())
                            .familyRelationship(relation.relationship())
                            .relationshipType(RelationshipType.FAMILY)
                            .build(),
                    auditInfo
            );
        });
    }

    private void syncRelations(Family family, List<FamilyPopDto.FamilyRelationship> relations) {
        enrolleeRelationService.deleteByFamilyId(family.getId(), DataAuditInfo.builder().systemProcess(
                DataAuditInfo.systemProcessName(getClass(), "populateFamily")).build());
        createRelations(family, relations);
    }

    @Override
    @Transactional
    public Family createPreserveExisting(Family existingObj, FamilyPopDto popDto, StudyPopulateContext context) {
        StudyEnvironment attachedEnv = studyEnvironmentService
                .findByStudy(context.getStudyShortcode(), context.getEnvironmentName()).get();

        Enrollee proband = enrolleeService.findOneByShortcode(popDto.getProbandShortcode()).orElseThrow();
        DataAuditInfo auditInfo = DataAuditInfo.builder().systemProcess(
                DataAuditInfo.systemProcessName(getClass(), "populateFamily")).build();

        Family family = familyService.update(
                Family
                        .builder()
                        .id(existingObj.getId())
                        .shortcode(popDto.getShortcode())
                        .probandEnrolleeId(proband.getId())
                        .studyEnvironmentId(attachedEnv.getId())
                        .build(),
                auditInfo
        );

        List<Enrollee> members = enrolleeService.findAllByShortcodes(popDto.getMemberShortcodes());
        syncFamilyMembers(family, members);

        syncRelations(family, popDto.getRelations());

        return family;
    }

    @Override
    @Transactional
    public Family overwriteExisting(Family existingObj, FamilyPopDto popDto, StudyPopulateContext context) {
        familyService.delete(existingObj.getId(), DataAuditInfo.builder().systemProcess(
                DataAuditInfo.systemProcessName(getClass(), "populateFamily")).build());
        return createNew(popDto, context, true);
    }

    @Override
    public Optional<Family> findFromDto(FamilyPopDto popDto, StudyPopulateContext context ) {
        return familyService.findOneByShortcode(popDto.getShortcode());
    }
}
