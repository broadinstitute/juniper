package bio.terra.pearl.core.service.participant;

import bio.terra.pearl.core.dao.participant.EnrolleeRelationDao;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.EnrolleeRelation;
import bio.terra.pearl.core.model.participant.Family;
import bio.terra.pearl.core.model.participant.RelationshipType;
import bio.terra.pearl.core.service.DataAuditedService;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.workflow.DataChangeRecordService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EnrolleeRelationService extends DataAuditedService<EnrolleeRelation, EnrolleeRelationDao> {
    private final EnrolleeService enrolleeService;
    private final ProfileService profileService;
    private final FamilyService familyService;

    public EnrolleeRelationService(EnrolleeRelationDao enrolleeRelationDao,
                                   DataChangeRecordService dataChangeRecordService,
                                   @Lazy EnrolleeService enrolleeService,
                                   ObjectMapper objectMapper,
                                   ProfileService profileService,
                                   @Lazy FamilyService familyService) {
        super(enrolleeRelationDao, dataChangeRecordService, objectMapper);
        this.enrolleeService = enrolleeService;
        this.profileService = profileService;
        this.familyService = familyService;
    }

    public List<EnrolleeRelation> findByEnrolleeIdAndRelationType(UUID enrolleeId, RelationshipType relationshipType) {
        return filterValid(dao.findByEnrolleeIdAndRelationshipType(enrolleeId, relationshipType));
    }

    public List<EnrolleeRelation> findByTargetEnrolleeIdAndRelationshipType(UUID enrolleeId, RelationshipType relationshipType) {
        return filterValid(dao.findByTargetEnrolleeIdAndRelationshipType(enrolleeId, relationshipType));
    }

    public List<EnrolleeRelation> findByEnrolleeIdsAndRelationType(List<UUID> enrolleeIds, RelationshipType relationshipType) {
        return filterValid(dao.findByEnrolleeIdsAndRelationshipType(enrolleeIds, relationshipType));
    }

    public List<EnrolleeRelation> findByTargetEnrolleeId(UUID enrolleeId) {
        return filterValid(dao.findByTargetEnrolleeId(enrolleeId));
    }

    public List<EnrolleeRelation> findAllByEnrolleeId(UUID enrolleeId) {
        return filterValid(dao.findAllByEnrolleeId(enrolleeId));
    }

    public List<EnrolleeRelation> findByTargetEnrolleeIdWithEnrolleesAndFamily(UUID enrolleeId) {
        List<EnrolleeRelation> relations = this.findByTargetEnrolleeIdWithEnrollees(enrolleeId);
        List<UUID> familyIds = relations
                .stream()
                .map(EnrolleeRelation::getFamilyId)
                .filter(Objects::nonNull)
                .distinct() // only grab unique family ids; likely to be repeats
                .toList();
        List<Family> families = familyService.findAll(familyIds);

        relations.forEach(relation -> {
            if (relation.getFamilyId() != null) {
                relation.setFamily(families
                        .stream()
                        .filter(family -> family.getId().equals(relation.getFamilyId()))
                        .findFirst()
                        .orElse(null));
            }
        });

        return relations;
    }
    
    public List<EnrolleeRelation> findAllByEnrolleeOrTargetId(UUID enrolleeId) {
        return filterValid(dao.findAllByEnrolleeOrTargetId(enrolleeId));
    }

    public List<EnrolleeRelation> findByTargetEnrolleeIdWithEnrollees(UUID enrolleeId) {
        Enrollee target = this.enrolleeService.find(enrolleeId).orElseThrow(() -> new NotFoundException("Enrollee not found"));
        profileService
                .loadWithMailingAddress(target.getProfileId())
                .ifPresent(target::setProfile);

        List<EnrolleeRelation> relations = findByTargetEnrolleeId(enrolleeId);

        return relations.stream().map(relation -> {
            relation.setTargetEnrollee(target);
            relation.setEnrollee(this.enrolleeService.find(relation.getEnrolleeId()).orElse(null));
            profileService
                    .loadWithMailingAddress(relation.getEnrollee().getProfileId())
                    .ifPresent(relation.getEnrollee()::setProfile);

            return relation;
        }).toList();
    }


    public boolean isUserProxyForAnyOf(UUID participantUserId, List<UUID> enrolleeIds) {
        return !dao.findEnrolleeRelationsByProxyParticipantUser(participantUserId, enrolleeIds)
                .stream().filter(enrolleeRelation -> isRelationshipValid(enrolleeRelation)).collect(Collectors.toList()).isEmpty();
    }

    public Optional<Enrollee> isUserProxyForEnrollee(UUID participantUserId, String enrolleeShortcode) {
        Enrollee targetEnrollee = enrolleeService.findOneByShortcode(enrolleeShortcode)
                .orElseThrow(() -> new NotFoundException("Enrollee with shortcode %s was not found ".formatted(enrolleeShortcode)));
        List<EnrolleeRelation> relations = dao.findEnrolleeRelationsByProxyParticipantUser(participantUserId, List.of(targetEnrollee.getId()));
        if (!validRelations(relations).isEmpty()) {
            return Optional.of(targetEnrollee);
        }
        return Optional.empty();
    }

    private List<EnrolleeRelation> validRelations(List<EnrolleeRelation> in) {
        return in.stream().filter(this::isRelationshipValid).toList();
    }

    public void attachTargetEnrollees(List<EnrolleeRelation> relations) {
        dao.attachTargetEnrollees(relations);
    }


    public boolean isRelationshipValid(EnrolleeRelation enrolleeRelation) {
        return (enrolleeRelation.getEndDate() == null || enrolleeRelation.getEndDate().isAfter(Instant.now()));
    }

    public void deleteAllByEnrolleeIdOrTargetId(UUID enrolleeId) {
        List<EnrolleeRelation> enrolleeRelations = dao.findAllByEnrolleeId(enrolleeId);
        enrolleeRelations.addAll(dao.findByTargetEnrolleeId(enrolleeId));
        bulkDelete(enrolleeRelations, DataAuditInfo.builder().systemProcess(DataAuditInfo.systemProcessName(getClass(),
                "deleteAllByEnrolleeIdOrTargetId")).build());
    }

    public void deleteAllFamilyRelationshipsByEnrolleeIdOrTargetIdAndFamilyId(UUID enrolleeId, UUID familyId, DataAuditInfo info) {
        List<EnrolleeRelation> enrolleeRelations = dao.findAllFamilyRelationshipsByEnrolleeIdOrTargetIdAndFamilyId(enrolleeId, familyId);
        bulkDelete(enrolleeRelations, info);
    }

    /**
     * This method returns a list of enrollees that are proxies only for this target user.
     * An enrollee is exclusively proxied if it is only proxied by the given enrollee and no other enrollees.
     * @param targetEnrolleeId the id of the target enrollee
     * @return a list of enrollees that are exclusively proxied by the given enrollee
     * */
    public List<Enrollee> findExclusiveProxiesForTargetEnrollee(UUID targetEnrolleeId) {
        List<EnrolleeRelation> enrolleeRelations = dao.findByTargetEnrolleeIdAndRelationshipType(targetEnrolleeId, RelationshipType.PROXY);
        List<Enrollee> exclusiveProxies = new ArrayList<>();
        for (EnrolleeRelation enrolleeRelation : enrolleeRelations) {
            if (findAllByEnrolleeId(enrolleeRelation.getEnrolleeId()).size() == 1) {
                enrolleeService.find(enrolleeRelation.getEnrolleeId()).ifPresent(exclusiveProxies::add);
            }
        }
        return exclusiveProxies;
    }

    public List<EnrolleeRelation> filterValid(List<EnrolleeRelation> enrolleeRelations) {
        return enrolleeRelations.stream().filter(this::isRelationshipValid).collect(Collectors.toList());
    }

    public List<EnrolleeRelation> findRelationsForFamily(UUID familyId) {
        return filterValid(dao.findRelationsForFamily(familyId));
    }

    @Transactional
    public void deleteByFamilyId(UUID id, DataAuditInfo info) {
        List<EnrolleeRelation> relations = findRelationsForFamily(id);
        bulkDelete(relations, info);
    }

    // WARNING: This method is not audited; it should only be used during study population/repopulation
    @Transactional
    public void deleteByStudyEnvironmentId(UUID studyEnvironmentId) {
        dao.deleteByStudyEnvironmentId(studyEnvironmentId);
    }

    public void attachEnrolleesAndFamily(EnrolleeRelation relation) {
        relation.setEnrollee(enrolleeService.find(relation.getEnrolleeId()).map(enrollee -> {
            enrolleeService.attachProfile(enrollee);
            return enrollee;
        }).orElse(null));
        relation.setTargetEnrollee(enrolleeService.find(relation.getTargetEnrolleeId()).map(enrollee -> {
            enrolleeService.attachProfile(enrollee);
            return enrollee;
        }).orElse(null));

        if (relation.getFamilyId() != null) {
            relation.setFamily(familyService.find(relation.getFamilyId()).orElse(null));
        }
    }
}
