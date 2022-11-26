package bio.terra.pearl.core.service;

import bio.terra.pearl.core.dao.PortalDao;
import bio.terra.pearl.core.model.Portal;
import bio.terra.pearl.core.model.PortalEnvironment;
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
    private PortalEnvironmentService portalEnvironmentService;
    private StudyService studyService;

    public PortalService(PortalDao portalDao, PortalStudyService portalStudyService, StudyService studyService) {
        this.portalDao = portalDao;
        this.portalStudyService = portalStudyService;
        this.studyService = studyService;
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
        portalDao.delete(portalId);
    }

    public Optional<Portal> findOneByShortcode(String shortcode) {
        return portalDao.findOneByShortcode(shortcode);
    }

    public enum AllowedCascades implements CascadeProperty {
        PORTAL_ENVIRONMENT,
        STUDY,
        PORTAL_STUDY;

    }
}
