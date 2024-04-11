package bio.terra.pearl.core.service.participant.search;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.dao.workflow.ParticipantTaskDao;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.StudyFactory;
import bio.terra.pearl.core.factory.portal.PortalFactory;
import bio.terra.pearl.core.factory.survey.SurveyFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.EnrolleeSearchFacet;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.workflow.TaskStatus;
import bio.terra.pearl.core.service.search.terms.SearchValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
                                                "name": "oh_oh_basic_middleInitial",
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


        Map<String, SearchValue.SearchValueType> results = searchService.getExpressionSearchFacetsForStudyEnv(bundle1.getStudyEnv().getId());

        Assertions.assertEquals(18, results.size());
        Assertions.assertEquals(Map.ofEntries(
                Map.entry("profile.givenName", SearchValue.SearchValueType.STRING),
                Map.entry("profile.familyName", SearchValue.SearchValueType.STRING),
                Map.entry("profile.contactEmail", SearchValue.SearchValueType.STRING),
                Map.entry("profile.phoneNumber", SearchValue.SearchValueType.STRING),
                Map.entry("profile.birthDate", SearchValue.SearchValueType.DATE),
                Map.entry("profile.mailingAddress.street1", SearchValue.SearchValueType.STRING),
                Map.entry("profile.mailingAddress.street2", SearchValue.SearchValueType.STRING),
                Map.entry("profile.mailingAddress.city", SearchValue.SearchValueType.STRING),
                Map.entry("profile.mailingAddress.state", SearchValue.SearchValueType.STRING),
                Map.entry("profile.mailingAddress.postalCode", SearchValue.SearchValueType.STRING),
                Map.entry("profile.mailingAddress.country", SearchValue.SearchValueType.STRING),
                Map.entry("answer.another_survey.question_1", SearchValue.SearchValueType.STRING),
                Map.entry("answer.another_survey.question_2", SearchValue.SearchValueType.STRING),
                Map.entry("answer.another_survey.question_3", SearchValue.SearchValueType.STRING),
                Map.entry("answer.test_survey_1.oh_oh_basic_firstName", SearchValue.SearchValueType.STRING),
                Map.entry("answer.test_survey_1.oh_oh_basic_lastName", SearchValue.SearchValueType.STRING),
                Map.entry("answer.test_survey_1.oh_oh_basic_middleInitial", SearchValue.SearchValueType.STRING),
                Map.entry("age", SearchValue.SearchValueType.INTEGER)
        ), results);

    }
}
