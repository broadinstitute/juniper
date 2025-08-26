package bio.terra.pearl.core.service.search.terms;

import bio.terra.pearl.core.dao.survey.AnswerDao;
import bio.terra.pearl.core.model.search.SearchValueTypeDefinition;
import bio.terra.pearl.core.model.survey.QuestionChoice;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.survey.SurveyQuestionDefinition;
import bio.terra.pearl.core.service.survey.SurveyService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.*;

import static bio.terra.pearl.core.service.search.terms.SearchValue.SearchValueType.STRING;

@Service
public class AnswerTermParser extends SearchTermParser<AnswerTerm> {
    private final AnswerDao answerDao;
    private final SurveyService surveyService;
    private final ObjectMapper objectMapper;

    public AnswerTermParser(ObjectMapper objectMapper, AnswerDao answerDao, SurveyService surveyService) {
        this.objectMapper = objectMapper;
        this.answerDao = answerDao;
        this.surveyService = surveyService;
    }

    @Override
    public AnswerTerm parse(String term) {
        List<String> arguments = splitArguments(term, 2);

        if (arguments.size() != 2) {
            throw new IllegalArgumentException("Answer terms must be in the format {answer.surveyStableId.questionStableId}. Instead, got: " + term);
        }

        return new AnswerTerm(answerDao, arguments.get(0), arguments.get(1));
    }

    @Override
    public AnswerTerm parse(String studyShortcode, String variables) {
        List<String> arguments = splitArguments(variables, 2);

        if (arguments.size() != 2) {
            throw new IllegalArgumentException("Answer terms must be in the format {answer.surveyStableId.questionStableId}. Instead, got: " + variables);
        }

        return new AnswerTerm(answerDao, studyShortcode, arguments.get(0), arguments.get(1));
    }

    @Override
    public String getTermName() {
        return "answer";
    }

    @Override
    public Map<String, SearchValueTypeDefinition> getFacets(UUID studyEnvId) {
        Map<String, SearchValueTypeDefinition> fields = new HashMap<>();

        List<Survey> surveys = surveyService.findByStudyEnvironmentIdWithContent(studyEnvId);
        for (Survey survey : surveys) {
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
