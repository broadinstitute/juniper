package bio.terra.pearl.core.service.survey;

import bio.terra.pearl.core.dao.survey.AnswerMappingDao;
import bio.terra.pearl.core.dao.survey.SurveyDao;
import bio.terra.pearl.core.dao.survey.SurveyQuestionDefinitionDao;
import bio.terra.pearl.core.dao.workflow.EventDao;
import bio.terra.pearl.core.model.survey.AnswerMapping;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.survey.SurveyQuestionDefinition;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.VersionedEntityService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;

@Service
public class SurveyService extends VersionedEntityService<Survey, SurveyDao> {
    private AnswerMappingDao answerMappingDao;
    private SurveyQuestionDefinitionDao surveyQuestionDefinitionDao;
    private EventDao eventDao;

    public SurveyService(SurveyDao surveyDao, AnswerMappingDao answerMappingDao, SurveyQuestionDefinitionDao surveyQuestionDefinitionDao, EventDao eventDao) {
        super(surveyDao);
        this.answerMappingDao = answerMappingDao;
        this.surveyQuestionDefinitionDao = surveyQuestionDefinitionDao;
        this.eventDao = eventDao;
    }

    public List<Survey> findByStableIdNoContent(String stableId) {
        return dao.findByStableIdNoContent(stableId);
    }

    public Optional<Survey> findByStableIdWithMappings(String stableId, int version) {
        return dao.findByStableIdWithMappings(stableId, version);
    }

    @Transactional
    @Override
    public void delete(UUID surveyId, Set<CascadeProperty> cascades) {
        answerMappingDao.deleteBySurveyId(surveyId);
        surveyQuestionDefinitionDao.deleteBySurveyId(surveyId);
        eventDao.deleteBySurveyId(surveyId);


        dao.delete(surveyId);
    }

    @Transactional
    @Override
    public Survey create(Survey survey) {
        Instant now = Instant.now();
        survey.setCreatedAt(now);
        survey.setLastUpdatedAt(now);
        Survey savedSurvey = dao.create(survey);
        for (AnswerMapping answerMapping : survey.getAnswerMappings()) {
            answerMapping.setSurveyId(savedSurvey.getId());
            AnswerMapping savedMapping = answerMappingDao.create(answerMapping);
            savedSurvey.getAnswerMappings().add(savedMapping);
        }
        List<SurveyQuestionDefinition> questionDefs = getSurveyQuestionDefinitions(savedSurvey);
        surveyQuestionDefinitionDao.bulkCreate(questionDefs);

        return savedSurvey;
    }

    public List<SurveyQuestionDefinition> getSurveyQuestionDefinitions(Survey survey) {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode surveyContent;

        try {
            surveyContent = objectMapper.readTree(survey.getContent());
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Malformed survey content json");
        }

        JsonNode pages = surveyContent.get("pages");
        if (pages == null) {
            // surveys should probably always have pages, but we want to not have this fail if the content is empty,
            // perhaps because it is a placeholder in-development survey
            pages = objectMapper.createArrayNode();
        }
        JsonNode templates = surveyContent.get("questionTemplates");

        //Some questions use templates. We need to gather those so that we can resolve
        //the types and possible choices for any question that uses one.
        Map<String, JsonNode> questionTemplates = new HashMap<>();
        if (templates != null) {
            for(JsonNode template : templates) {
                questionTemplates.put(template.get("name").asText(), template);
            }
        }

        //For each page in the survey, iterate through the JsonNode tree and unroll any panels
        List<JsonNode> questions = new ArrayList<>();
        for (JsonNode page : pages) {
            questions.addAll(SurveyParseUtils.getAllQuestions(page));
        }

        //Unmarshal the questions into actual definitions that we can store in the DB.
        //If the question uses a template, resolve that.
        List<SurveyQuestionDefinition> questionDefinitions = new ArrayList<>();
        for (int i = 0; i < questions.size(); i++) {
            JsonNode question = questions.get(i);
            questionDefinitions.add(SurveyParseUtils.unmarshalSurveyQuestion(survey, question,
                    questionTemplates, i,false));
        }

        // add any questions from calculatedValues
        processCalculatedValues(survey, surveyContent, questionDefinitions);

        return questionDefinitions;
    }

    public List<Survey> findByPortalId(UUID portalId) {
        return dao.findByPortalId(portalId);
    }

    /**
     * parses calculatedValues from the surveyContent and adds them as SurveyQuestionDefinitions
     * to the appropriate place in the questionDefinitions array.  they will be attempted to be
     * inserted following the question they are computed from
     */
    protected void processCalculatedValues(Survey survey,
                                           JsonNode surveyContent,
                                           List<SurveyQuestionDefinition> questionDefinitions) {
        List<JsonNode> derivedQuestions = SurveyParseUtils.getCalculatedValues(surveyContent);
        if (derivedQuestions.isEmpty()) {
            return;
        }
        /**
         * iterate through each question, inserting it into questionDefinitions following a
         * question on which it is derived from
         */
        for (JsonNode derivedQuestion : derivedQuestions) {
            String upstreamStableId = SurveyParseUtils.getUpstreamStableId(derivedQuestion);
            // the upstream index is either the occurrence of the question this calculatedValue depends on,
            // or the last item in the list if there is no dependency
            int upstreamIndex = IntStream.range(0, questionDefinitions.size())
                    .filter(i -> questionDefinitions.get(i).getQuestionStableId().equals(upstreamStableId))
                    .findFirst().orElse(questionDefinitions.size() - 1);
            questionDefinitions.add(upstreamIndex + 1,
                    SurveyParseUtils.unmarshalSurveyQuestion(survey, derivedQuestion,
                            Map.of(), upstreamIndex + 1,true));
        }
        // reassign the export orders
        IntStream.range(0, questionDefinitions.size()).forEach(i -> {
            questionDefinitions.get(i).setExportOrder(i);
        });
    }

    @Transactional
    public void deleteByPortalId(UUID portalId) {
        List<Survey> surveys = dao.findByPortalId(portalId);
        for (Survey survey : surveys) {
            delete(survey.getId(), new HashSet<>());
        }
    }

    /**
     * create a new version of the given survey with updated content.  the version will be the next
     * available number for the given stableId.
     * AnswerMappings from the prior survey will not be auto-carried forward -- they must be passed along with the
     * new survey.
     * */
    @Transactional
    public Survey createNewVersion(UUID portalId, Survey survey) {
        Survey newSurvey = new Survey();
        BeanUtils.copyProperties(survey, newSurvey, "id", "createdAt", "lastUpdatedAt", "answerMappings", "publishedVersion");
        newSurvey.setPortalId(portalId);
        int nextVersion = dao.getNextVersion(survey.getStableId());
        newSurvey.setVersion(nextVersion);
        newSurvey.getAnswerMappings().clear();
        for (AnswerMapping answerMapping : survey.getAnswerMappings()) {
            // we need to clone the answer mappings and attach them to the new version
            AnswerMapping newAnswerMapping = new AnswerMapping();
            BeanUtils.copyProperties(answerMapping, newAnswerMapping, "id", "createdAt", "lastUpdatedAt");
            newSurvey.getAnswerMappings().add(newAnswerMapping);
        }
        return create(newSurvey);
    }

    public void attachAnswerMappings(Survey survey) {
        survey.setAnswerMappings(answerMappingDao.findBySurveyId(survey.getId()));
    }


}
