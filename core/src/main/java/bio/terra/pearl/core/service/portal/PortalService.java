package bio.terra.pearl.core.service.portal;

import bio.terra.pearl.core.dao.portal.PortalDao;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.CascadeTree;
import bio.terra.pearl.core.service.study.PortalStudyService;
import bio.terra.pearl.core.service.study.StudyService;
import bio.terra.pearl.core.service.participant.ParticipantUserService;
import bio.terra.pearl.core.service.participant.PortalParticipantUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PortalService {
    private PortalDao portalDao;
    private PortalStudyService portalStudyService;
    private PortalParticipantUserService portalParticipantUserService;
    private PortalEnvironmentService portalEnvironmentService;
    private ParticipantUserService participantUserService;
    private StudyService studyService;

    public PortalService(PortalDao portalDao, PortalStudyService portalStudyService,
                         PortalParticipantUserService portalParticipantUserService, StudyService studyService,
                         PortalEnvironmentService portalEnvironmentService, ParticipantUserService participantUserService) {
        this.portalDao = portalDao;
        this.portalStudyService = portalStudyService;
        this.portalParticipantUserService = portalParticipantUserService;
        this.portalEnvironmentService = portalEnvironmentService;
        this.studyService = studyService;
        this.participantUserService = participantUserService;
    }

    @Transactional
    public Portal create(Portal portal) {
        Portal newPortal = portalDao.create(portal);

        portal.getPortalEnvironments().forEach(portalEnvironment -> {
            portalEnvironment.setPortalId(newPortal.getId());
            PortalEnvironment newEnv = portalEnvironmentService.create(portalEnvironment);
            newPortal.getPortalEnvironments().add(newEnv);
        });
        return newPortal;
    }

    @Transactional
    public void delete(UUID portalId, CascadeTree cascades) {
        List<UUID> studyIds = portalStudyService
                .findByPortalId(portalId).stream().map(portalStudy -> portalStudy.getStudyId())
                        .collect(Collectors.toList());
        portalStudyService.deleteByPortalId(portalId);
        if (cascades.hasProperty(AllowedCascades.STUDY)) {
            studyService.deleteOrphans(studyIds, cascades.getChild(AllowedCascades.STUDY));
        }

        List<UUID> participantUserIds = portalParticipantUserService
                .findByPortalId(portalId).stream().map(pUser -> pUser.getParticipantUserId())
                .collect(Collectors.toList());
        portalParticipantUserService.deleteByPortalId(portalId);
        if (cascades.hasProperty(AllowedCascades.PARTICIPANT_USER)) {
            participantUserService.deleteOrphans(participantUserIds, cascades.getChild(AllowedCascades.STUDY));
        }
        portalDao.delete(portalId);
    }

    public Optional<Portal> findOneByShortcode(String shortcode) {
        return portalDao.findOneByShortcode(shortcode);
    }

    public enum AllowedCascades implements CascadeProperty {
        PARTICIPANT_USER,
        PORTAL_ENVIRONMENT,
        STUDY,
        PORTAL_STUDY;

    }
}
