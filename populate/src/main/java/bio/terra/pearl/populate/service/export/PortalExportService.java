package bio.terra.pearl.populate.service.export;

import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.study.PortalStudy;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.portal.PortalEnvironmentService;
import bio.terra.pearl.core.service.portal.PortalService;
import bio.terra.pearl.core.service.study.*;
import bio.terra.pearl.core.service.study.exception.StudyEnvironmentMissing;
import bio.terra.pearl.populate.service.contexts.FilePopulateContext;
import org.springframework.stereotype.Service;

import java.util.List;

/** Exports all configurations for a portal */
@Service
public class PortalExportService {
    private final StudyEnvironmentService studyEnvironmentService;
    private final PortalService portalService;
    private final PortalEnvironmentService portalEnvironmentService;
    private final StudyService studyService;

    public PortalExportService(StudyEnvironmentService studyEnvironmentService,
                               PortalService portalService,
                               PortalEnvironmentService portalEnvironmentService, StudyService studyService) {
        this.studyEnvironmentService = studyEnvironmentService;
        this.portalService = portalService;
        this.portalEnvironmentService = portalEnvironmentService;
        this.studyService = studyService;
    }

    public void export(String portalShortcode) {
        Portal portal = portalService.findOneByShortcode(portalShortcode)
                .orElseThrow(() -> new NotFoundException("Portal not found: " + portalShortcode));
        attachAllContent(portal);
        ExportContext context = new ExportContext();
    }

    /** does a full load of all configs into memory, with the exception of images */
    protected Portal attachAllContent(Portal portal) {
        portalService.attachStudies(List.of(portal));
        for (PortalStudy portalStudy : portal.getPortalStudies()) {
            Study study = portalStudy.getStudy();
            studyEnvironmentService.findByStudy(study.getId()).stream().forEach(studyEnv -> {
                studyEnvironmentService.attachAllContent(studyEnv);
                study.getStudyEnvironments().add(studyEnv);
            });
        }
        portalEnvironmentService.findByPortal(portal.getId()).stream().forEach(portalEnv -> {
            portalEnvironmentService.attachAllContent(portalEnv);
            portal.getPortalEnvironments().add(portalEnv);
        });
        return portal;
    }
}
