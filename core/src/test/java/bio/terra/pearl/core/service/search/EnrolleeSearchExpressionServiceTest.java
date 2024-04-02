package bio.terra.pearl.core.service.search;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.StudyFactory;
import bio.terra.pearl.core.factory.portal.PortalFactory;
import bio.terra.pearl.core.factory.survey.SurveyFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.service.search.terms.SearchValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

class EnrolleeSearchExpressionServiceTest extends BaseSpringBootTest {

    @Autowired
    private EnrolleeSearchExpressionService enrolleeSearchExpressionService;

    @Autowired
    private PortalFactory portalFactory;

    @Autowired
    private SurveyFactory surveyFactory;

    @Autowired
    private StudyEnvironmentFactory studyEnvironmentFactory;

    @Autowired
    private StudyFactory studyFactory;

    @Test
    @Transactional
    void testGetSearchFacetsForPortal(TestInfo info) {
        Portal portal = portalFactory.buildPersisted(getTestName(info));
        Study study = studyFactory.buildPersisted(portal.getId(), getTestName(info));
        StudyEnvironmentFactory.StudyEnvironmentBundle bundle = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.sandbox, portal, study);

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


        surveyFactory.attachToEnv(survey1, bundle.getStudyEnv().getId(), true);
        surveyFactory.attachToEnv(survey2, bundle.getStudyEnv().getId(), true);


        Map<String, SearchValue.SearchValueType> results = enrolleeSearchExpressionService.getSearchFacetsForPortal(portal.getShortcode(), EnvironmentName.sandbox);

        /**
         * Expected
         *         {answer.another_survey.question_1=STRING, answer.another_survey.question_2=STRING, answer.another_survey.question_3=STRING, answer.test_survey_1.oh_oh_basic_middleInitial=STRING, profile.mailingAddress.state=STRING, profile.contactEmail=STRING, profile.mailingAddress.street2=STRING, profile.mailingAddress.street1=STRING, profile.phoneNumber=STRING, profile.birthDate=DATE, answer.test_survey_1.oh_oh_basic_lastName=STRING, answer.test_survey_1.oh_oh_basic_firstName=STRING, profile.mailingAddress.city=STRING, profile.givenName=STRING, profile.mailingAddress.postalCode=STRING, profile.familyName=STRING, age=INTEGER, profile.mailingAddress.country=STRING}
         */
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