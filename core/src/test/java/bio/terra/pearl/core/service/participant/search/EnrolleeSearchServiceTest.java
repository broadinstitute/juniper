package bio.terra.pearl.core.service.participant.search;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.dao.workflow.ParticipantTaskDao;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.StudyFactory;
import bio.terra.pearl.core.factory.portal.PortalFactory;
import bio.terra.pearl.core.factory.survey.SurveyFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.kit.KitRequestStatus;
import bio.terra.pearl.core.model.participant.EnrolleeSearchFacet;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.search.SearchValueTypeDefinition;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.survey.QuestionChoice;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.workflow.TaskStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static bio.terra.pearl.core.service.search.terms.SearchValue.SearchValueType.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class EnrolleeSearchServiceTest extends BaseSpringBootTest {
    @Autowired
    private EnrolleeSearchService searchService;
    @MockBean
    private ParticipantTaskDao mockParticipantTaskDao;

    @Autowired
    private PortalFactory portalFactory;

    @Autowired
    private SurveyFactory surveyFactory;

    @Autowired
    private StudyEnvironmentFactory studyEnvironmentFactory;

    @Autowired
    private StudyFactory studyFactory;

    @Test
    void testGetTaskFacet() {
        List<ParticipantTaskDao.EnrolleeTasks> tasks = List.of(
            ParticipantTaskDao.EnrolleeTasks.builder().targetName("Consent").targetStableId("consent").build(),
            ParticipantTaskDao.EnrolleeTasks.builder().targetName("Survey").targetStableId("survey").build()
        );
        when(mockParticipantTaskDao.findTaskNamesByStudy(any())).thenReturn(tasks);

        StudyEnvironment studyEnvironment = StudyEnvironment.builder().studyId(UUID.randomUUID()).build();
        EnrolleeSearchFacet facet = searchService.getTaskFacet(studyEnvironment);
        assertThat(facet.getKeyName(), equalTo("status"));
        assertThat(facet.getCategory(), equalTo("participantTask"));
        assertThat(facet.getFacetType(), equalTo(EnrolleeSearchFacet.FacetType.ENTITY_OPTIONS));

        Set<String> facetEntityValues = facet.getEntities().stream()
                .map(EnrolleeSearchFacet.ValueLabel::getValue).collect(Collectors.toSet());
        assertThat(facetEntityValues, equalTo(Set.of("consent", "survey")));

        // ensure that facet task types are in sync with TaskStatus enum
        Set<String> taskTypes = Arrays.stream(TaskStatus.values()).map(Enum::name).collect(Collectors.toSet());
        Set<String> facetTaskTypes = facet.getOptions().stream()
                .map(EnrolleeSearchFacet.ValueLabel::getValue).collect(Collectors.toSet());
        assertThat(taskTypes, equalTo(facetTaskTypes));
    }

    @Test
    @Transactional
    void testGetSearchFacetsForPortal(TestInfo info) {
        Portal portal = portalFactory.buildPersisted(getTestName(info));
        Study study = studyFactory.buildPersisted(portal.getId(), getTestName(info));
        StudyEnvironmentFactory.StudyEnvironmentBundle bundle1 = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.sandbox, portal, study);
        StudyEnvironment se2 = studyEnvironmentFactory.buildPersisted(bundle1.getPortalEnv(), getTestName(info));

        Survey survey1 = surveyFactory.buildPersisted(
                surveyFactory
                        .builder(getTestName(info))
                        .portalId(portal.getId())
                        .stableId("test_survey_1")
                        .content("""
                                {
                                    "title": "The Basics",
                                    "showQuestionNumbers": "off",
                                    "showProgressBar": "bottom",
                                    "pages": [
                                      {
                                        "elements": [
                                          {
                                            "type": "panel",
                                            "elements": [
                                            ]
                                          },
                                          {
                                            "type": "panel",
                                            "elements": [
                                              {
                                                "name": "oh_oh_basic_firstName",
                                                "type": "text",
                                                "title": "First name",
                                                "isRequired": true
                                              },
                                              {
                                                "name": "oh_oh_basic_lastName",
                                                "type": "text",
                                                "title": "Last name",
                                                "isRequired": true
                                              },
                                              {
                                                "name": "oh_oh_basic_country",
                                                "type": "text",
                                                "title": "Middle initial",
                                                "choices": [
                                                   {"value": "US", "text": "United States"},
                                                   {"value": "CA", "text": "Canada"}
                                                ]
                                              }
                                            ]
                                          }
                                        ]
                                      }
                                    ]
                                  }
                                """)
        );

        Survey survey2 = surveyFactory.buildPersisted(
                surveyFactory
                        .builder(getTestName(info))
                        .portalId(portal.getId())
                        .stableId("another_survey")
                        .content("""
                                {
                                    "title": "The Basics",
                                    "showQuestionNumbers": "off",
                                    "showProgressBar": "bottom",
                                    "pages": [
                                      {
                                        "elements": [
                                          {
                                            "type": "panel",
                                            "elements": [
                                            ]
                                          },
                                          {
                                            "type": "panel",
                                            "elements": [
                                              {
                                                "name": "question_1",
                                                "type": "text",
                                                "title": "First name",
                                                "isRequired": true
                                              },
                                              {
                                                "name": "question_2",
                                                "type": "text",
                                                "title": "Last name",
                                                "isRequired": true
                                              },
                                              {
                                                "name": "question_3",
                                                "type": "text",
                                                "title": "Middle initial",
                                                "maxLength": 1,
                                                "size": 1
                                              }
                                            ]
                                          }
                                        ]
                                      }
                                    ]
                                  }
                                """)
        );

        Survey survey3 = surveyFactory.buildPersisted(
                surveyFactory
                        .builder(getTestName(info))
                        .portalId(portal.getId())
                        .stableId("survey_in_diff_study")
                        .content("""
                                {
                                    "title": "The Basics",
                                    "showQuestionNumbers": "off",
                                    "showProgressBar": "bottom",
                                    "pages": [
                                      {
                                        "elements": [
                                        {
                                            "type": "panel",
                                            "elements": [
                                              {
                                                "name": "question_1",
                                                "type": "text",
                                                "title": "First name",
                                                "isRequired": true
                                              }
                                            ]
                                          }
                                        ]
                                      }
                                    ]
                                  }
                                """)
        );


        surveyFactory.attachToEnv(survey1, bundle1.getStudyEnv().getId(), true);
        surveyFactory.attachToEnv(survey2, bundle1.getStudyEnv().getId(), true);
        surveyFactory.attachToEnv(survey3, se2.getId(), true);


        Map<String, SearchValueTypeDefinition> results = searchService.getExpressionSearchFacetsForStudyEnv(bundle1.getStudyEnv().getId());

        List<QuestionChoice> taskStatusChoices = Arrays.stream(TaskStatus.values())
                .map(val -> new QuestionChoice(val.name(), val.name()))
                .collect(Collectors.toList());
        List<QuestionChoice> kitStatusChoices = Arrays.stream(KitRequestStatus.values())
                .map(val -> new QuestionChoice(val.name(), val.name()))
                .collect(Collectors.toList());

        Assertions.assertEquals(28, results.size());
        Map.ofEntries(
                Map.entry("profile.givenName", SearchValueTypeDefinition.builder().type(STRING).build()),
                Map.entry("profile.familyName", SearchValueTypeDefinition.builder().type(STRING).build()),
                Map.entry("profile.name", SearchValueTypeDefinition.builder().type(STRING).build()),
                Map.entry("profile.contactEmail", SearchValueTypeDefinition.builder().type(STRING).build()),
                Map.entry("profile.phoneNumber", SearchValueTypeDefinition.builder().type(STRING).build()),
                Map.entry("profile.birthDate", SearchValueTypeDefinition.builder().type(DATE).build()),
                Map.entry("profile.sexAtBirth", SearchValueTypeDefinition.builder().type(STRING).build()),
                Map.entry("profile.mailingAddress.street1", SearchValueTypeDefinition.builder().type(STRING).build()),
                Map.entry("profile.mailingAddress.street2", SearchValueTypeDefinition.builder().type(STRING).build()),
                Map.entry("profile.mailingAddress.city", SearchValueTypeDefinition.builder().type(STRING).build()),
                Map.entry("profile.mailingAddress.state", SearchValueTypeDefinition.builder().type(STRING).build()),
                Map.entry("profile.mailingAddress.postalCode", SearchValueTypeDefinition.builder().type(STRING).build()),
                Map.entry("profile.mailingAddress.country", SearchValueTypeDefinition.builder().type(STRING).build()),
                Map.entry("answer.another_survey.question_1", SearchValueTypeDefinition.builder().type(STRING).build()),
                Map.entry("answer.another_survey.question_2", SearchValueTypeDefinition.builder().type(STRING).build()),
                Map.entry("answer.another_survey.question_3", SearchValueTypeDefinition.builder().type(STRING).build()),
                Map.entry("answer.test_survey_1.oh_oh_basic_firstName", SearchValueTypeDefinition.builder().type(STRING).build()),
                Map.entry("answer.test_survey_1.oh_oh_basic_lastName", SearchValueTypeDefinition.builder().type(STRING).build()),
                Map.entry("answer.test_survey_1.oh_oh_basic_country",
                        SearchValueTypeDefinition.builder().type(STRING)
                                .choices(
                                        List.of(new QuestionChoice("US", "United States"),
                                                new QuestionChoice("CA", "Canada"))
                                ).build()),
                Map.entry("task.another_survey.status", SearchValueTypeDefinition.builder().type(STRING).choices(taskStatusChoices).build()),
                Map.entry("task.another_survey.assigned", SearchValueTypeDefinition.builder().type(BOOLEAN).build()),
                Map.entry("task.test_survey_1.assigned", SearchValueTypeDefinition.builder().type(BOOLEAN).build()),
                Map.entry("task.test_survey_1.status", SearchValueTypeDefinition.builder().type(STRING).choices(taskStatusChoices).build()),
                Map.entry("enrollee.subject", SearchValueTypeDefinition.builder().type(BOOLEAN).build()),
                Map.entry("enrollee.consented", SearchValueTypeDefinition.builder().type(BOOLEAN).build()),
                Map.entry("enrollee.shortcode", SearchValueTypeDefinition.builder().type(STRING).build()),
                Map.entry("age", SearchValueTypeDefinition.builder().type(NUMBER).build()),
                Map.entry("latestKit.status", SearchValueTypeDefinition.builder().type(STRING).choices(kitStatusChoices).build())
        ).forEach((key, value) -> {
            Assertions.assertTrue(results.containsKey(key), "Key not found: " + key);
            Assertions.assertEquals(value, results.get(key), "Wrong value for key: " + key + ", expected: " + value + " got: " + results.get(key));
        });

    }
}
