package bio.terra.pearl.api.admin.service.forms;

import static org.junit.jupiter.api.Assertions.assertEquals;

import bio.terra.pearl.api.admin.BaseSpringBootTest;
import bio.terra.pearl.core.factory.portal.PortalFactory;
import bio.terra.pearl.core.factory.survey.SurveyFactory;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.survey.AnswerMapping;
import bio.terra.pearl.core.model.survey.AnswerMappingMapType;
import bio.terra.pearl.core.model.survey.AnswerMappingTargetType;
import bio.terra.pearl.core.model.survey.Survey;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;

class AnswerMappingExtServiceTest extends BaseSpringBootTest {

  @Autowired private AnswerMappingExtService answerMappingExtService;
  @Autowired private SurveyFactory surveyFactory;
  @Autowired private PortalFactory portalFactory;

  @Test
  void testCrud(TestInfo info) {
    Portal portal = portalFactory.buildPersisted(getTestName(info));
    Survey survey = surveyFactory.buildPersisted(getTestName(info), portal.getId());

    String portalShortcode = portal.getShortcode();
    String stableId = survey.getStableId();
    Integer version = survey.getVersion();

    AdminUser operator = AdminUser.builder().superuser(true).build();
    // Create
    AnswerMapping answerMapping1 =
        answerMappingExtService.createAnswerMappingForSurvey(
            operator,
            AnswerMapping.builder()
                .mapType(AnswerMappingMapType.STRING_TO_STRING)
                .questionStableId("question_given_name")
                .targetField("given_name")
                .targetType(AnswerMappingTargetType.PROFILE)
                .errorOnFail(false)
                .formatString("")
                .build(),
            portalShortcode,
            stableId,
            version);

    AnswerMapping answerMapping2 =
        answerMappingExtService.createAnswerMappingForSurvey(
            operator,
            AnswerMapping.builder()
                .mapType(AnswerMappingMapType.STRING_TO_STRING)
                .questionStableId("question_family_name")
                .targetField("family_name")
                .targetType(AnswerMappingTargetType.PROFILE)
                .errorOnFail(false)
                .formatString("")
                .build(),
            portalShortcode,
            stableId,
            version);

    // Read
    List<AnswerMapping> mappings =
        answerMappingExtService.findBySurvey(operator, portalShortcode, stableId, version);
    assertEquals(2, mappings.size());
    assertEquals(answerMapping1, mappings.get(0));
    assertEquals(answerMapping2, mappings.get(1));

    // Delete
    answerMappingExtService.deleteAnswerMapping(
        operator, portalShortcode, stableId, version, answerMapping1.getId());

    mappings = answerMappingExtService.findBySurvey(operator, portalShortcode, stableId, version);
    assertEquals(1, mappings.size());
    assertEquals(answerMapping2, mappings.get(0));
  }
}
