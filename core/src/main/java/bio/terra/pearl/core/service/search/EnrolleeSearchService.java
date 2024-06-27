package bio.terra.pearl.core.service.search;

import bio.terra.pearl.core.dao.search.EnrolleeSearchExpressionDao;
import bio.terra.pearl.core.model.search.EnrolleeSearchExpressionResult;
import bio.terra.pearl.core.model.search.SearchValueTypeDefinition;
import bio.terra.pearl.core.model.survey.QuestionChoice;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.survey.SurveyQuestionDefinition;
import bio.terra.pearl.core.service.search.terms.*;
import bio.terra.pearl.core.service.survey.SurveyService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;
import org.postgresql.util.PSQLException;
import org.springframework.stereotype.Service;

import java.util.*;

import static bio.terra.pearl.core.service.search.terms.SearchValue.SearchValueType.NUMBER;
import static bio.terra.pearl.core.service.search.terms.SearchValue.SearchValueType.STRING;

@Service
public class EnrolleeSearchService {
    private final EnrolleeSearchExpressionDao enrolleeSearchExpressionDao;
    private final SurveyService surveyService;
    private final EnrolleeSearchExpressionParser enrolleeSearchExpressionParser;
    private final ObjectMapper objectMapper;


    public EnrolleeSearchService(EnrolleeSearchExpressionDao enrolleeSearchExpressionDao,
                                 SurveyService surveyService,
                                 EnrolleeSearchExpressionParser enrolleeSearchExpressionParser,
                                 ObjectMapper objectMapper) {
        this.enrolleeSearchExpressionDao = enrolleeSearchExpressionDao;
        this.surveyService = surveyService;
        this.enrolleeSearchExpressionParser = enrolleeSearchExpressionParser;
        this.objectMapper = objectMapper;
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
        fields.put("age", SearchValueTypeDefinition.builder().type(NUMBER).build());
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
                                convertQuestionDefinitionToSearchType(def));
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
        } catch (UnableToExecuteStatementException e) {
            String message = e.getShortMessage();

            // PSQLException has the most useful error message, so we should
            // see if we can grab it
            if (e.getCause().getClass().equals(PSQLException.class)) {
                PSQLException psqlException = (PSQLException) e.getCause();
                if (Objects.nonNull(psqlException.getServerErrorMessage())) {
                    message = psqlException.getServerErrorMessage().getMessage();
                }
            }

            throw new IllegalArgumentException("Invalid search expression: " + message);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid search expression: " + e.getMessage());
        }

    }

    public SearchValueTypeDefinition convertQuestionDefinitionToSearchType(SurveyQuestionDefinition def) {
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
                .type(getSearchValueType(def))
                .allowMultiple(def.isAllowMultiple())
                .build();
    }

    private SearchValue.SearchValueType getSearchValueType(SurveyQuestionDefinition def) {
        return STRING;
    }
}
