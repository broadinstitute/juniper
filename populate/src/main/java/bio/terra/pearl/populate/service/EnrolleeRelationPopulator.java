package bio.terra.pearl.populate.service;

import java.io.IOException;
import java.util.Optional;

import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.EnrolleeRelation;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.participant.RelationshipType;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.participant.EnrolleeRelationService;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.ParticipantUserService;
import bio.terra.pearl.core.service.participant.PortalParticipantUserService;
import bio.terra.pearl.populate.dto.participant.EnrolleeRelationPopDto;
import bio.terra.pearl.populate.service.contexts.StudyPopulateContext;
import org.springframework.stereotype.Service;

@Service
public class EnrolleeRelationPopulator  extends BasePopulator<EnrolleeRelation, EnrolleeRelationPopDto, StudyPopulateContext> {
    private PortalParticipantUserService portalParticipantUserService;
    private ParticipantUserService participantUserService;
    private EnrolleeRelationService enrolleeRelationService;
    private EnrolleeService enrolleeService;

    public EnrolleeRelationPopulator(PortalParticipantUserService portalParticipantUserService,
                                     ParticipantUserService participantUserService,
                                     EnrolleeRelationService enrolleeRelationService,
                                     EnrolleeService enrolleeService){
        this.portalParticipantUserService = portalParticipantUserService;
        this.participantUserService = participantUserService;
        this.enrolleeRelationService = enrolleeRelationService;
        this.enrolleeService = enrolleeService;
    }

    @Override
    protected Class<EnrolleeRelationPopDto> getDtoClazz() {
        return EnrolleeRelationPopDto.class;
    }

    @Override
    public Optional<EnrolleeRelation> findFromDto(EnrolleeRelationPopDto popDto, StudyPopulateContext context) {
        EnrolleeRelation enrolleeRelation = buildEnrolleRerlation(popDto, context);
        return enrolleeRelationService.findByEnrolleeIdAndًُRelationType(enrolleeRelation.getEnrolleeId(), enrolleeRelation.getRelationshipType()).stream().filter(enrolleeRelation1 -> enrolleeRelation1.getParticipantUserId().equals(enrolleeRelation.getParticipantUserId())).findAny();
    }

    @Override
    public EnrolleeRelation overwriteExisting(EnrolleeRelation existingObj, EnrolleeRelationPopDto popDto, StudyPopulateContext context)
            throws IOException {
        enrolleeRelationService.delete(existingObj.getId(), CascadeProperty.EMPTY_SET);
        return createNew(popDto, context, true);
    }

    @Override
    public EnrolleeRelation createPreserveExisting(EnrolleeRelation existingObj, EnrolleeRelationPopDto popDto,
                                                   StudyPopulateContext context) throws IOException {
        // we don't support preserving existing synthetic enrolleeRelations yet
        enrolleeRelationService.delete(existingObj.getId(), CascadeProperty.EMPTY_SET);
        return createNew(popDto, context, true);
    }

    @Override
    public EnrolleeRelation createNew(EnrolleeRelationPopDto popDto, StudyPopulateContext context, boolean overwrite) throws IOException {
        EnrolleeRelation enrolleeRelation = buildEnrolleRerlation(popDto, context);
        enrolleeRelationService.create(enrolleeRelation);
        return enrolleeRelation;
    }

    public EnrolleeRelation buildEnrolleRerlation(EnrolleeRelationPopDto popDto, StudyPopulateContext context){
        ParticipantUser attachedGovernedUser = participantUserService
                .findOne(popDto.getLinkedEnrolleeUsername(), context.getEnvironmentName()).get();
        PortalParticipantUser governedUser = portalParticipantUserService
                .findOne(attachedGovernedUser.getId(), context.getPortalShortcode()).get();
        String enrolleeShortCode = popDto.getEnrolleeShortCode();
        ParticipantUser attachedProxy = participantUserService
                .findOne(popDto.getLinkedParticipantUsername(), context.getEnvironmentName()).get();
        RelationshipType relationshipType = popDto.getRelationshipType();

        Enrollee enrollee = enrolleeService.findByParticipantUserId(governedUser.getParticipantUserId(), enrolleeShortCode).orElseThrow(()-> new NotFoundException("enrollee not found"));

        EnrolleeRelation enrolleeRelation = EnrolleeRelation.builder()
                .enrolleeId(enrollee.getId())
                .participantUserId(attachedProxy.getId())
                .relationshipType(relationshipType)
                .build();
        return enrolleeRelation;
    }

}
