package bio.terra.pearl.api.admin.service;

import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.kit.KitType;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.study.StudyEnvironmentConfig;
import bio.terra.pearl.core.service.kit.StudyKitTypeService;
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
  private final StudyKitTypeService studyKitTypeService;
  private final StudyService studyService;
  private final PortalStudyService portalStudyService;
  private final SiteContentService siteContentService;

  public StudyExtService(
      AuthUtilService authUtilService,
      StudyKitTypeService studyKitTypeService,
      StudyService studyService,
      PortalStudyService portalStudyService,
      SiteContentService siteContentService) {
    this.authUtilService = authUtilService;
    this.studyKitTypeService = studyKitTypeService;
    this.studyService = studyService;
    this.portalStudyService = portalStudyService;
    this.siteContentService = siteContentService;
  }

  public List<KitType> getKitTypes(
      AdminUser operator, String portalShortcode, String studyShortcode) {
    var portalStudy = authUtilService.authUserToStudy(operator, portalShortcode, studyShortcode);
    return studyKitTypeService.findKitTypesByStudyId(portalStudy.getStudyId());
  }

  @Transactional
  public Study create(String portalShortcode, StudyCreationDto study, AdminUser operator) {
    Portal portal = authUtilService.authUserToPortal(operator, portalShortcode);
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
