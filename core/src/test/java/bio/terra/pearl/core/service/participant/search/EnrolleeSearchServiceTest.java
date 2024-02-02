package bio.terra.pearl.core.service.participant.search;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.dao.workflow.ParticipantTaskDao;
import bio.terra.pearl.core.model.participant.EnrolleeSearchFacet;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.workflow.TaskStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

class EnrolleeSearchServiceTest extends BaseSpringBootTest {
    @Autowired
    private EnrolleeSearchService searchService;
    @MockBean
    private ParticipantTaskDao mockParticipantTaskDao;

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
}
