package bio.terra.pearl.api.admin.service.forms;

import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.survey.AnswerMapping;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.survey.AnswerMappingService;
import bio.terra.pearl.core.service.survey.SurveyService;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class AnswerMappingExtService {
  private final AnswerMappingService answerMappingService;
  private final AuthUtilService authUtilService;
  private final SurveyService surveyService;

  public AnswerMappingExtService(
      AnswerMappingService answerMappingService,
      AuthUtilService authUtilService,
      SurveyService surveyService) {
    this.answerMappingService = answerMappingService;
    this.authUtilService = authUtilService;
    this.surveyService = surveyService;
  }

  public List<AnswerMapping> findBySurvey(
      AdminUser operator, String portalShortcode, String stableId, Integer version) {
    authUtilService.authUserToPortal(operator, portalShortcode);
    Survey survey =
        surveyService
            .findByStableIdAndPortalShortcode(stableId, version, portalShortcode)
            .orElseThrow(() -> new NotFoundException("Survey not found"));
    return answerMappingService.findBySurveyId(survey.getId());
  }

  public AnswerMapping createAnswerMappingForSurvey(
      AdminUser operator,
      AnswerMapping mapping,
      String portalShortcode,
      String stableId,
      Integer version) {
    authUtilService.authUserToPortal(operator, portalShortcode);
    Survey survey =
        surveyService
            .findByStableIdAndPortalShortcode(stableId, version, portalShortcode)
            .orElseThrow(() -> new NotFoundException("Survey not found"));

    mapping.setSurveyId(survey.getId());
    return answerMappingService.create(mapping);
  }

  public void deleteAnswerMapping(
      AdminUser operator,
      String portalShortcode,
      String stableId,
      Integer version,
      UUID answerMappingId) {
    authUtilService.authUserToPortal(operator, portalShortcode);
    Survey survey =
        surveyService
            .findByStableIdAndPortalShortcode(stableId, version, portalShortcode)
            .orElseThrow(() -> new NotFoundException("Survey not found"));

    AnswerMapping mapping =
        answerMappingService
            .find(answerMappingId)
            .orElseThrow(() -> new NotFoundException("Answer mapping not found"));

    if (!mapping.getSurveyId().equals(survey.getId())) {
      throw new NotFoundException("Answer mapping not found");
    }

    answerMappingService.delete(answerMappingId, CascadeProperty.EMPTY_SET);
  }
}
