package bio.terra.pearl.core.service.survey;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.DaoTestUtils;
import bio.terra.pearl.core.factory.admin.AdminUserFactory;
import bio.terra.pearl.core.factory.survey.SurveyFactory;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.survey.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class SurveyServiceTests extends BaseSpringBootTest {
    @Autowired
    private SurveyService surveyService;
    @Autowired
    private SurveyFactory surveyFactory;
    @Autowired
    private AdminUserFactory adminUserFactory;

    @Test
    @Transactional
    public void testCreateSurvey() {
        Survey survey = surveyFactory.builder("testPublishSurvey").build();
        survey.setCreatedAt(null);
        survey.setLastUpdatedAt(null);
        Survey savedSurvey = surveyService.create(survey);
        DaoTestUtils.assertGeneratedProperties(savedSurvey);
        Assertions.assertEquals(savedSurvey.getName(), survey.getName());

        Survey fetchedSurvey = surveyService.findByStableId(savedSurvey.getStableId(), savedSurvey.getVersion()).get();
        Assertions.assertEquals(fetchedSurvey.getId(), savedSurvey.getId());
    }

    @Test
    @Transactional
    public void testCreateSurveyWithMappings() {
        Survey survey = surveyFactory.builder("testPublishSurvey").build();
        AnswerMapping answerMapping = AnswerMapping.builder()
                .questionStableId("qStableId")
                .targetField("givenName")
                .mapType(AnswerMappingMapType.STRING_TO_STRING)
                .targetType(AnswerMappingTargetType.PROFILE)
                .build();
        survey.getAnswerMappings().add(answerMapping);
        Survey savedSurvey = surveyService.create(survey);
        assertThat(savedSurvey.getAnswerMappings(), hasSize(1));
        DaoTestUtils.assertGeneratedProperties(savedSurvey.getAnswerMappings().get(0));

        Survey fetchedSurvey = surveyService.findByStableIdWithMappings(savedSurvey.getStableId(), savedSurvey.getVersion()).get();
        assertThat(fetchedSurvey.getAnswerMappings(), hasSize(1));
    }

    @Test
    @Transactional
    public void testCreateNewVersion() {
        Survey survey = surveyFactory.buildPersisted("testCreateNewVersion");
        String oldContent = survey.getContent();
        String newContent = String.format("{\"pages\":[],\"title\":\"%s\"}", RandomStringUtils.randomAlphabetic(6));
        survey.setContent(newContent);
        Survey newSurvey = surveyService.createNewVersion(survey.getPortalId(), survey);

        Assertions.assertNotEquals(newSurvey.getId(), survey.getId());
        // check version was incremented and content was modified
        Assertions.assertEquals(survey.getVersion() + 1, newSurvey.getVersion());
        Assertions.assertEquals(newContent, newSurvey.getContent());

        // confirm the existing survey wasn't modified
        Survey fetchedOriginal = surveyService.find(survey.getId()).get();
        Assertions.assertEquals(oldContent, fetchedOriginal.getContent());
    }

    @Test
    @Transactional
    public void testAssignPublishedVersion() {
        Survey survey = surveyFactory.buildPersisted("testAssignPublishedVersion");
        surveyService.assignPublishedVersion(survey.getId());
        survey = surveyService.find(survey.getId()).get();
        assertThat(survey.getPublishedVersion(), equalTo(1));

        String newContent = String.format("{\"pages\":[],\"title\":\"%s\"}", RandomStringUtils.randomAlphabetic(6));
        survey.setContent(newContent);
        Survey newSurvey = surveyService.createNewVersion(survey.getPortalId(), survey);

        Assertions.assertNotEquals(newSurvey.getId(), survey.getId());
        // check published version was NOT copied
        assertThat(newSurvey.getPublishedVersion(), equalTo(null));

        surveyService.assignPublishedVersion(newSurvey.getId());
        newSurvey = surveyService.find(newSurvey.getId()).get();
        assertThat(newSurvey.getPublishedVersion(), equalTo(2));
    }


    @Test
    @Transactional
    public void testCreateNewVersionWithMappings() {
        AnswerMapping answerMapping = AnswerMapping.builder()
                .questionStableId("qStableId")
                .targetField("givenName")
                .mapType(AnswerMappingMapType.STRING_TO_STRING)
                .targetType(AnswerMappingTargetType.PROFILE)
                .build();
        Survey survey = surveyFactory.buildPersisted("testPublishSurvey", List.of(answerMapping));
        AdminUser user = adminUserFactory.buildPersisted("testPublishSurvey");
        String oldContent = survey.getContent();
        String newContent = String.format("{\"pages\":[],\"title\":\"%s\"}", RandomStringUtils.randomAlphabetic(6));
        survey.setContent(newContent);

        AnswerMapping answerMappingUpdate = AnswerMapping.builder()
                .questionStableId("qStableId")
                .targetField("lastName")
                .mapType(AnswerMappingMapType.STRING_TO_STRING)
                .targetType(AnswerMappingTargetType.PROFILE)
                .build();
        survey.getAnswerMappings().clear();
        survey.getAnswerMappings().add(answerMappingUpdate);
        Survey newSurvey = surveyService.createNewVersion(survey.getPortalId(), survey);
        surveyService.attachAnswerMappings(newSurvey);
        // check version was incremented and answer mappings were saved too
        Assertions.assertEquals(survey.getVersion() + 1, newSurvey.getVersion());
        Assertions.assertEquals(1, newSurvey.getAnswerMappings().size());
        Assertions.assertEquals("lastName", newSurvey.getAnswerMappings().get(0).getTargetField());

        // confirm old version answer mappings weren't changed
        surveyService.attachAnswerMappings(survey);
        Assertions.assertEquals(1, survey.getAnswerMappings().size());
        Assertions.assertEquals("givenName", survey.getAnswerMappings().get(0).getTargetField());

    }

    @Test
    public void testGetSurveyQuestionDefinitions() {
        Survey survey = surveyFactory.buildPersisted("testPublishSurvey");
        String surveyContent = """
                {
                	"title": "The Basics",
                	"showQuestionNumbers": "off",
                	"pages": [{
                		"elements": [{
                			"name": "oh_oh_basic_firstName",
                			"type": "text",
                			"title": "First name",
                			"isRequired": true
                		}, {
                			"name": "oh_oh_basic_mghPatient",
                			"type": "radiogroup",
                			"title": "Are you a patient, current or former, of MGH?",
                			"isRequired": true,
                			"choices": [{
                				"text": "Yes",
                				"value": "yes"
                			}, {
                				"text": "No",
                				"value": "no"
                			}]
                		}]
                	}, {
                		"elements": [{
                			"name": "oh_oh_basic_streetAddress",
                			"type": "text",
                			"title": "Address",
                			"isRequired": true
                		}, {
                			"name": "oh_oh_basic_city",
                			"type": "text",
                			"title": "City",
                			"isRequired": true
                		}]
                	}]
                }""";

        survey.setContent(surveyContent);

        List<SurveyQuestionDefinition> actual = surveyService.getSurveyQuestionDefinitions(survey);
        Assertions.assertEquals(4, actual.size());
    }

}
