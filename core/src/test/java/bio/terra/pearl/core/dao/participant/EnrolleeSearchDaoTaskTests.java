package bio.terra.pearl.core.dao.participant;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.kit.KitRequestFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.factory.participant.ParticipantTaskFactory;
import bio.terra.pearl.core.factory.portal.PortalEnvironmentFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.EnrolleeSearchResult;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.workflow.TaskStatus;
import bio.terra.pearl.core.model.workflow.TaskType;
import bio.terra.pearl.core.service.participant.ParticipantUserService;
import bio.terra.pearl.core.service.participant.search.facets.CombinedStableIdFacetValue;
import bio.terra.pearl.core.service.participant.search.facets.StableIdStringFacetValue;
import bio.terra.pearl.core.service.participant.search.facets.sql.ParticipantTaskFacetSqlGenerator;
import bio.terra.pearl.core.service.participant.search.facets.sql.SqlSearchableFacet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

public class EnrolleeSearchDaoTaskTests extends BaseSpringBootTest {
    @Autowired
    private EnrolleeFactory enrolleeFactory;
    @Autowired
    private StudyEnvironmentFactory studyEnvironmentFactory;
    @Autowired
    private PortalEnvironmentFactory portalEnvironmentFactory;
    @Autowired
    private ParticipantTaskFactory participantTaskFactory;
    @Autowired
    private EnrolleeSearchDao enrolleeSearchDao;

    @Test
    @Transactional
    public void testTaskSearch(TestInfo info) throws Exception {
        PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted(getTestName(info), EnvironmentName.live);
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(portalEnv, getTestName(info));
        // enrollee who has completed both surveys
        EnrolleeFactory.EnrolleeBundle doneEnrolleeBundle = enrolleeFactory.buildWithPortalUser(getTestName(info), portalEnv, studyEnv);
        participantTaskFactory.buildPersisted(doneEnrolleeBundle, "bigSurvey", TaskStatus.COMPLETE, TaskType.SURVEY);
        participantTaskFactory.buildPersisted(doneEnrolleeBundle, "otherSurvey", TaskStatus.COMPLETE, TaskType.SURVEY);

        // enrollee who has only  one survey in progress
        EnrolleeFactory.EnrolleeBundle inProgressEnrolleeBundle = enrolleeFactory.buildWithPortalUser(getTestName(info), portalEnv, studyEnv);
        participantTaskFactory.buildPersisted(inProgressEnrolleeBundle, "bigSurvey", TaskStatus.IN_PROGRESS, TaskType.SURVEY);

        // enrollee with no tasks
        EnrolleeFactory.EnrolleeBundle untaskedEnrolleeBundle = enrolleeFactory.buildWithPortalUser(getTestName(info), portalEnv, studyEnv);

        // enrollee who has only completed the big survey
        EnrolleeFactory.EnrolleeBundle oneSurveyEnrollee = enrolleeFactory.buildWithPortalUser(getTestName(info), portalEnv, studyEnv);
        participantTaskFactory.buildPersisted(oneSurveyEnrollee, "bigSurvey", TaskStatus.COMPLETE, TaskType.SURVEY);

        SqlSearchableFacet facet = new SqlSearchableFacet(new CombinedStableIdFacetValue("status",
                List.of(new StableIdStringFacetValue("status", "bigSurvey", List.of("COMPLETE")))), new ParticipantTaskFacetSqlGenerator());
        List<EnrolleeSearchResult> result = enrolleeSearchDao.search(studyEnv.getId(), List.of(facet));
        assertThat(result, hasSize(2));
        assertThat(result.stream().map(resultMap -> resultMap.getEnrollee().getShortcode()).toList(),
                hasItems(doneEnrolleeBundle.enrollee().getShortcode(),oneSurveyEnrollee.enrollee().getShortcode() ));

        SqlSearchableFacet otherFacet = new SqlSearchableFacet(new CombinedStableIdFacetValue("status",
                List.of(new StableIdStringFacetValue("status", "otherSurvey", List.of("COMPLETE")))), new ParticipantTaskFacetSqlGenerator());

        List<EnrolleeSearchResult> otherResult = enrolleeSearchDao.search(studyEnv.getId(), List.of(otherFacet));
        assertThat(otherResult, hasSize(1));
        assertThat(otherResult.get(0).getEnrollee().getShortcode(), equalTo(doneEnrolleeBundle.enrollee().getShortcode()));


        SqlSearchableFacet bothSurveyFacet = new SqlSearchableFacet(new CombinedStableIdFacetValue("status",
                List.of(new StableIdStringFacetValue("status", "bigSurvey", List.of("COMPLETE")),
                        new StableIdStringFacetValue("status", "otherSurvey", List.of("COMPLETE")))), new ParticipantTaskFacetSqlGenerator());
        List<EnrolleeSearchResult> bothSurveyResult = enrolleeSearchDao.search(studyEnv.getId(), List.of(bothSurveyFacet));
        assertThat(bothSurveyResult, hasSize(1));
        assertThat(bothSurveyResult.get(0).getEnrollee().getShortcode(), equalTo(doneEnrolleeBundle.enrollee().getShortcode()));
    }

}
