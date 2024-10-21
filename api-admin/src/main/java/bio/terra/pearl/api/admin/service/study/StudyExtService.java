package bio.terra.pearl.api.admin.service.study;

import bio.terra.pearl.api.admin.models.dto.StudyCreationDto;
import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.api.admin.service.auth.EnforcePortalPermission;
import bio.terra.pearl.api.admin.service.auth.context.PortalAuthContext;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.study.PortalStudy;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.study.StudyEnvironmentConfig;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.exception.PermissionDeniedException;
import bio.terra.pearl.core.service.exception.internal.InternalServerException;
import bio.terra.pearl.core.service.kit.StudyEnvironmentKitTypeService;
import bio.terra.pearl.core.service.portal.PortalService;
import bio.terra.pearl.core.service.site.SiteContentService;
import bio.terra.pearl.core.service.study.PortalStudyService;
import bio.terra.pearl.core.service.study.StudyService;
import bio.terra.pearl.populate.dto.StudyPopDto;
import bio.terra.pearl.populate.service.FilePopulateService;
import bio.terra.pearl.populate.service.StudyPopulator;
import bio.terra.pearl.populate.service.contexts.PortalPopulateContext;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudyExtService {
  private final AuthUtilService authUtilService;
  private final StudyEnvironmentKitTypeService studyEnvironmentKitTypeService;
  private final StudyService studyService;
  private final PortalStudyService portalStudyService;
  private final PortalService portalService;
  private final SiteContentService siteContentService;
  private final StudyPopulator studyPopulator;
  private final FilePopulateService filePopulateService;

  public StudyExtService(
      AuthUtilService authUtilService,
      StudyEnvironmentKitTypeService studyEnvironmentKitTypeService,
      StudyService studyService,
      PortalStudyService portalStudyService,
      SiteContentService siteContentService,
      PortalService portalService,
      StudyPopulator studyPopulator,
      FilePopulateService filePopulateService) {
    this.authUtilService = authUtilService;
    this.studyEnvironmentKitTypeService = studyEnvironmentKitTypeService;
    this.studyService = studyService;
    this.portalStudyService = portalStudyService;
    this.siteContentService = siteContentService;
    this.portalService = portalService;
    this.studyPopulator = studyPopulator;
    this.filePopulateService = filePopulateService;
  }

  @Transactional
  public Study create(String portalShortcode, StudyCreationDto study, AdminUser operator) {
    if (!operator.isSuperuser()) {
      throw new PermissionDeniedException("You do not have permission to create studies");
    }
    Portal portal = authUtilService.authUserToPortal(operator, portalShortcode);
    /** Create empty environments for each of sandbox, irb, and live */
    List<StudyEnvironment> studyEnvironments =
        Arrays.stream(EnvironmentName.values())
            .map(envName -> makeEmptyEnvironment(envName, envName == EnvironmentName.sandbox))
            .toList();

    Study newStudy =
        Study.builder()
            .shortcode(study.getShortcode())
            .name(study.getName())
            .studyEnvironments(studyEnvironments)
            .build();
    newStudy = studyService.create(newStudy);
    portalStudyService.create(portal.getId(), newStudy.getId());

    if (Objects.nonNull(study.getTemplate())) {
      fillInWithTemplate(portalShortcode, newStudy, study.getTemplate());
    }

    return newStudy;
  }

  /** gets all the studies for a portal, with the environments attached */
  @EnforcePortalPermission(permission = "BASE")
  public List<Study> getStudiesWithEnvs(PortalAuthContext authContext, EnvironmentName envName) {
    List<Study> studies = studyService.findByPortalId(authContext.getPortal().getId());
    studies.forEach(
        study -> {
          studyService.attachEnvironments(study);
          study.setStudyEnvironments(
              study.getStudyEnvironments().stream()
                  .filter(env -> envName == null || env.getEnvironmentName().equals(envName))
                  .toList());
        });
    return studies;
  }

  private void fillInWithTemplate(
      String portalShortcode, Study newStudy, StudyCreationDto.StudyTemplate studyTemplate) {
    String filename;

    switch (studyTemplate) {
      default -> {
        filename = "basic_study.json";
      }
    }

    PortalPopulateContext config =
        new PortalPopulateContext(
            "templates/" + filename, portalShortcode, null, new HashMap<>(), false, null);

    StudyPopDto studyPopDto;

    try {
      String fileContents = filePopulateService.readFile(filename, config);
      studyPopDto = studyPopulator.readValue(fileContents);

      studyPopDto.setShortcode(newStudy.getShortcode());
      studyPopDto.setName(newStudy.getName());

      studyPopulator.populateFromDto(studyPopDto, config, false);
    } catch (IOException e) {
      throw new InternalServerException("Failed to pre-populate study.");
    }
  }

  @Transactional
  public void delete(String portalShortcode, String studyShortcode, AdminUser operator) {
    if (!operator.isSuperuser()) {
      throw new PermissionDeniedException("You do not have permission to delete studies");
    }
    Portal portal =
        portalService
            .findOneByShortcodeOrHostname(portalShortcode)
            .orElseThrow(
                () ->
                    new NotFoundException(
                        "Portal "
                            + portalShortcode
                            + " was not found as one of the portals for study "
                            + studyShortcode));
    PortalStudy portalStudy =
        portalStudyService
            .findStudyInPortal(studyShortcode, portal.getId())
            .orElseThrow(
                () ->
                    new NotFoundException(
                        "Portal "
                            + portalShortcode
                            + " was not found as one of the portals for study "
                            + studyShortcode));
    portalStudyService.deleteByStudyId(portalStudy.getStudyId());
    studyService.delete(portalStudy.getStudyId(), CascadeProperty.EMPTY_SET);
  }

  /**
   * we make empty environments as placeholders for the environment views. This minimizes the amount
   * of hardcoding we have to do in the UI around sandbox/irb/prod, giving us the flexibility to add
   * more alternate environments in the future
   */
  private StudyEnvironment makeEmptyEnvironment(EnvironmentName envName, boolean initialized) {
    StudyEnvironment studyEnv =
        StudyEnvironment.builder()
            .environmentName(envName)
            .studyEnvironmentConfig(
                StudyEnvironmentConfig.builder().initialized(initialized).build())
            .build();
    return studyEnv;
  }
}
