package bio.terra.pearl.core.service.participant.search;

import bio.terra.pearl.core.dao.participant.EnrolleeSearchDao;
import bio.terra.pearl.core.dao.workflow.ParticipantTaskDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.EnrolleeSearchFacet;
import bio.terra.pearl.core.model.participant.EnrolleeSearchResult;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.service.participant.search.facets.sql.SqlSearchableFacet;
import bio.terra.pearl.core.service.portal.PortalService;
import bio.terra.pearl.core.service.search.terms.ProfileTerm;
import bio.terra.pearl.core.service.search.terms.SearchValue;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.study.exception.StudyEnvironmentMissing;
import bio.terra.pearl.core.service.survey.SurveyService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class EnrolleeSearchService {
    private final EnrolleeSearchDao enrolleeSearchDao;
    private final ParticipantTaskDao participantTaskDao;
    private final StudyEnvironmentService studyEnvironmentService;
    private final PortalService portalService;
    private final SurveyService surveyService;

    public EnrolleeSearchService(EnrolleeSearchDao enrolleeSearchDao, ParticipantTaskDao participantTaskDao, StudyEnvironmentService studyEnvironmentService, PortalService portalService, SurveyService surveyService) {
        this.enrolleeSearchDao = enrolleeSearchDao;
        this.participantTaskDao = participantTaskDao;
        this.studyEnvironmentService = studyEnvironmentService;
        this.portalService = portalService;
        this.surveyService = surveyService;
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
        List<ParticipantTaskDao.EnrolleeTasks> tasks = participantTaskDao.findTaskNamesByStudy(studyEnvironment.getId());

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

    public Map<String, SearchValue.SearchValueType> getExpressionSearchFacetsForStudyEnv(UUID studyEnvId) {

        Map<String, SearchValue.SearchValueType> fields = new HashMap<>();
        // profile fields
        ProfileTerm.FIELDS.forEach((term, type) -> fields.put("profile." + term, type));
        // age
        fields.put("age", SearchValue.SearchValueType.INTEGER);
        // answers
        List<Survey> surveys = surveyService.findByStudyEnvironmentIdWithContent(studyEnvId);
        for (Survey survey : surveys) {
            surveyService
                    .getSurveyQuestionDefinitions(survey)
                    .forEach(def -> {
                        fields.put("answer." + def.getSurveyStableId() + "." + def.getQuestionStableId(), SearchValue.SearchValueType.STRING);
                    });
        }

        return fields;
    }
}
