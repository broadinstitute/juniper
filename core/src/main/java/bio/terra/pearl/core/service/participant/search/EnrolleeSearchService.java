package bio.terra.pearl.core.service.participant.search;

import bio.terra.pearl.core.dao.participant.EnrolleeSearchDao;
import bio.terra.pearl.core.dao.workflow.ParticipantTaskDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.EnrolleeSearchFacet;
import bio.terra.pearl.core.model.participant.EnrolleeSearchResult;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.service.participant.search.facets.sql.SqlSearchableFacet;
import java.util.List;
import java.util.Map;

import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.study.exception.StudyEnvironmentMissing;
import org.springframework.stereotype.Service;

@Service
public class EnrolleeSearchService {
    private final EnrolleeSearchDao enrolleeSearchDao;
    private final ParticipantTaskDao participantTaskDao;
    private final StudyEnvironmentService studyEnvironmentService;

    public EnrolleeSearchService(EnrolleeSearchDao enrolleeSearchDao, ParticipantTaskDao participantTaskDao, StudyEnvironmentService studyEnvironmentService) {
        this.enrolleeSearchDao = enrolleeSearchDao;
        this.participantTaskDao = participantTaskDao;
        this.studyEnvironmentService = studyEnvironmentService;
    }

    public List<EnrolleeSearchResult> search(String studyShortcode, EnvironmentName envName,
                                            List<SqlSearchableFacet> facets) {

        return enrolleeSearchDao.search(studyShortcode, envName, facets);
    }

    public List<EnrolleeSearchFacet> getFacets(String studyShortcode, EnvironmentName envName) {
        StudyEnvironment studyEnvironment = studyEnvironmentService
                .findByStudy(studyShortcode, envName).orElseThrow(StudyEnvironmentMissing::new);
        List<ParticipantTaskDao.EnrolleeTasks> tasks = participantTaskDao.findTasksByStudy(studyEnvironment.getId());

        EnrolleeSearchFacet tasksFacet = new EnrolleeSearchFacet("status", EnrolleeSearchFacet.FacetType.STABLEID_STRING, "participantTask", "Task status");
        tasks.forEach(task -> tasksFacet.addEntity(task.getTargetStableId(), task.getTargetName()));

        Map<String, String> status = Map.of(
                "NEW", "Not started",
                "IN_PROGRESS", "In progress",
                "COMPLETED", "Completed"
        );
        tasksFacet.addOptions(status);
        return List.of(tasksFacet);
    }
}
