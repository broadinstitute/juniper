package bio.terra.pearl.core.service.participant.search;

import bio.terra.pearl.core.dao.participant.EnrolleeSearchDao;
import bio.terra.pearl.core.dao.search.EnrolleeSearchExpressionDao;
import bio.terra.pearl.core.dao.workflow.ParticipantTaskDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.EnrolleeSearchFacet;
import bio.terra.pearl.core.model.participant.EnrolleeSearchResult;
import bio.terra.pearl.core.model.search.EnrolleeSearchExpressionResult;
import bio.terra.pearl.core.model.search.SearchValueTypeDefinition;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.survey.QuestionChoice;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.survey.SurveyQuestionDefinition;
import bio.terra.pearl.core.service.participant.search.facets.sql.SqlSearchableFacet;
import bio.terra.pearl.core.service.portal.PortalService;
import bio.terra.pearl.core.service.search.EnrolleeSearchExpressionParser;
import bio.terra.pearl.core.service.search.terms.EnrolleeTerm;
import bio.terra.pearl.core.service.search.terms.LatestKitTerm;
import bio.terra.pearl.core.service.search.terms.ProfileTerm;
import bio.terra.pearl.core.service.search.terms.TaskTerm;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.study.exception.StudyEnvironmentMissing;
import bio.terra.pearl.core.service.survey.SurveyService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.*;

import static bio.terra.pearl.core.service.search.terms.SearchValue.SearchValueType.INTEGER;
import static bio.terra.pearl.core.service.search.terms.SearchValue.SearchValueType.STRING;

@Service
public class EnrolleeSearchService {
    private final EnrolleeSearchDao enrolleeSearchDao;
    private final EnrolleeSearchExpressionDao enrolleeSearchExpressionDao;
    private final ParticipantTaskDao participantTaskDao;
    private final StudyEnvironmentService studyEnvironmentService;
    private final SurveyService surveyService;
    private final EnrolleeSearchExpressionParser enrolleeSearchExpressionParser;
    private final ObjectMapper objectMapper;

    public EnrolleeSearchService(EnrolleeSearchDao enrolleeSearchDao, EnrolleeSearchExpressionDao enrolleeSearchExpressionDao, ParticipantTaskDao participantTaskDao, StudyEnvironmentService studyEnvironmentService, PortalService portalService, SurveyService surveyService, EnrolleeSearchExpressionParser enrolleeSearchExpressionParser, ObjectMapper objectMapper) {
        this.enrolleeSearchDao = enrolleeSearchDao;
        this.enrolleeSearchExpressionDao = enrolleeSearchExpressionDao;
        this.participantTaskDao = participantTaskDao;
        this.studyEnvironmentService = studyEnvironmentService;
        this.surveyService = surveyService;
        this.enrolleeSearchExpressionParser = enrolleeSearchExpressionParser;
        this.objectMapper = objectMapper;
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

    public Map<String, SearchValueTypeDefinition> getExpressionSearchFacetsForStudyEnv(UUID studyEnvId) {

        Map<String, SearchValueTypeDefinition> fields = new HashMap<>();
        // profile fields
        ProfileTerm.FIELDS.forEach((term, type) -> fields.put("profile." + term, type));
        // enrollee fields
        EnrolleeTerm.FIELDS.forEach((term, type) -> fields.put("enrollee." + term, type));
        // latest kit fields
        LatestKitTerm.FIELDS.forEach((term, type) -> fields.put("latestKit." + term, type));
        // age
        fields.put("age", SearchValueTypeDefinition.builder().type(INTEGER).build());
        // answers
        List<Survey> surveys = surveyService.findByStudyEnvironmentIdWithContent(studyEnvId);
        for (Survey survey : surveys) {
            // task fields
            TaskTerm.FIELDS.forEach((term, type) -> fields.put("task." + survey.getStableId() + "." + term, type));
            // answer fields
            surveyService
                    .getSurveyQuestionDefinitions(survey)
                    .forEach(def -> {
                        fields.put(
                                "answer." + def.getSurveyStableId() + "." + def.getQuestionStableId(),
                                fromQuestionDefinition(def));
                    });
        }

        return fields;
    }

    public List<EnrolleeSearchExpressionResult> executeSearchExpression(UUID studyEnvId, String expression) {
        try {
            return enrolleeSearchExpressionDao.executeSearch(
                    enrolleeSearchExpressionParser.parseRule(expression),
                    studyEnvId
            );
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid search expression: " + e.getMessage());
        }

    }

    private SearchValueTypeDefinition fromQuestionDefinition(SurveyQuestionDefinition def) {
        SearchValueTypeDefinition.SearchValueTypeDefinitionBuilder<?, ?> builder = SearchValueTypeDefinition.builder();

        if (Objects.nonNull(def.getChoices()) && !def.getChoices().isEmpty()) {
            List<QuestionChoice> choices = new ArrayList<>();
            try {
                choices = objectMapper.readValue(def.getChoices(), new TypeReference<List<QuestionChoice>>() {
                        })
                        .stream()
                        .map(choice -> {
                            if (Objects.isNull(choice.stableId()) || choice.stableId().isEmpty()) {
                                return new QuestionChoice(choice.text(), choice.text());
                            }
                            if (Objects.isNull(choice.text()) || choice.text().isEmpty()) {
                                return new QuestionChoice(choice.stableId(), choice.stableId());
                            }
                            return choice;
                        })
                        .toList();
            } catch (Exception e) {
                // ignore
            }
            builder.choices(choices);
        }

        return builder
                .allowOtherDescription(def.isAllowOtherDescription())
                .type(STRING)
                .allowMultiple(def.isAllowMultiple())
                .build();
    }
}
