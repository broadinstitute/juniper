package bio.terra.pearl.core.service.participant.search;

import bio.terra.pearl.core.dao.participant.EnrolleeSearchDao;
import bio.terra.pearl.core.dao.workflow.ParticipantTaskDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.EnrolleeSearchFacet;
import bio.terra.pearl.core.model.participant.EnrolleeSearchResult;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.service.participant.search.facets.sql.SqlSearchableFacet;

import java.util.LinkedHashMap;
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

        // currently only returns task facets
        return List.of(getTaskFacet(studyEnvironment));
    }

    protected EnrolleeSearchFacet getTaskFacet(StudyEnvironment studyEnvironment) {
        List<ParticipantTaskDao.EnrolleeTasks> tasks = participantTaskDao.findTasksByStudy(studyEnvironment.getId());

        EnrolleeSearchFacet tasksFacet = new EnrolleeSearchFacet("status",
                EnrolleeSearchFacet.FacetType.ENTITY_OPTIONS, "participantTask", "Task status");
        tasks.forEach(task -> tasksFacet.addEntity(task.getTargetStableId(), task.getTargetName()));

        // These mirror the values in TaskStatus, but it does not seem appropriate to add
        // this map alongside TaskStatus enum since the text may not be universally applicable.
        // These values are verified in sync via an automated test
        Map<String, String> status = new LinkedHashMap<>();
        status.put("NEW", "Not started");
        status.put("VIEWED", "Viewed");
        status.put("IN_PROGRESS", "In progress");
        status.put("COMPLETE", "Completed");
        status.put("REJECTED", "Rejected");

        tasksFacet.addOptions(status);
        return tasksFacet;
    }
}
