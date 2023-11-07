package bio.terra.pearl.api.admin.service.study;

import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.study.StudyEnvironmentConfig;
import bio.terra.pearl.core.service.exception.PermissionDeniedException;
import bio.terra.pearl.core.service.kit.StudyEnvironmentKitTypeService;
import bio.terra.pearl.core.service.site.SiteContentService;
import bio.terra.pearl.core.service.study.PortalStudyService;
import bio.terra.pearl.core.service.study.StudyService;
import java.util.Arrays;
import java.util.List;
import lombok.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudyExtService {
  private final AuthUtilService authUtilService;
  private final StudyEnvironmentKitTypeService studyEnvironmentKitTypeService;
  private final StudyService studyService;
  private final PortalStudyService portalStudyService;
  private final SiteContentService siteContentService;

  public StudyExtService(
      AuthUtilService authUtilService,
      StudyEnvironmentKitTypeService studyEnvironmentKitTypeService,
      StudyService studyService,
      PortalStudyService portalStudyService,
      SiteContentService siteContentService) {
    this.authUtilService = authUtilService;
    this.studyEnvironmentKitTypeService = studyEnvironmentKitTypeService;
    this.studyService = studyService;
    this.portalStudyService = portalStudyService;
    this.siteContentService = siteContentService;
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
    return newStudy;
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

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class StudyCreationDto {
    private String shortcode;
    private String name;
  }
}
