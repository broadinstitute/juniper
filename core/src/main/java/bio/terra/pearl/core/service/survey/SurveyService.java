package bio.terra.pearl.core.service.survey;

import bio.terra.pearl.core.dao.survey.AnswerMappingDao;
import bio.terra.pearl.core.dao.survey.SurveyDao;
import bio.terra.pearl.core.dao.survey.SurveyQuestionDefinitionDao;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.survey.AnswerMapping;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.survey.SurveyQuestionDefinition;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.ImmutableEntityService;
import bio.terra.pearl.core.service.VersionedEntityService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SurveyService extends ImmutableEntityService<Survey, SurveyDao> implements VersionedEntityService<Survey> {
    private AnswerMappingDao answerMappingDao;
    private SurveyQuestionDefinitionDao surveyQuestionDefinitionDao;

    public SurveyService(SurveyDao surveyDao, AnswerMappingDao answerMappingDao, SurveyQuestionDefinitionDao surveyQuestionDefinitionDao) {
        super(surveyDao);
        this.answerMappingDao = answerMappingDao;
        this.surveyQuestionDefinitionDao = surveyQuestionDefinitionDao;
    }

    public Optional<Survey> findByStableId(String stableId, int version) {
        return dao.findByStableId(stableId, version);
    }

    public Optional<Survey> findByStableIdWithMappings(String stableId, int version) {
        return dao.findByStableIdWithMappings(stableId, version);
    }

    @Transactional
    @Override
    public void delete(UUID surveyId, Set<CascadeProperty> cascades) {
        answerMappingDao.deleteBySurveyId(surveyId);
        surveyQuestionDefinitionDao.deleteBySurveyId(surveyId);
        dao.delete(surveyId);
    }

    @Transactional
    @Override
    public Survey create(Survey survey) {
        Survey savedSurvey = dao.create(survey);
        for (AnswerMapping answerMapping : survey.getAnswerMappings()) {
            answerMapping.setSurveyId(savedSurvey.getId());
            AnswerMapping savedMapping = answerMappingDao.create(answerMapping);
            savedSurvey.getAnswerMappings().add(savedMapping);
        }

        for(SurveyQuestionDefinition questionDefinition : getSurveyQuestionDefinitions(savedSurvey)) {
            surveyQuestionDefinitionDao.create(questionDefinition);
        }

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
        for (JsonNode question : questions) {
            questionDefinitions.add(SurveyParseUtils.unmarshalSurveyQuestion(survey, question, questionTemplates));
        }

        return questionDefinitions;
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
    public Survey createNewVersion(AdminUser user, UUID portalId, Survey survey) {
        // TODO check user permissions
        Survey newSurvey = new Survey();
        BeanUtils.copyProperties(survey, newSurvey, "id", "createdAt", "lastUpdatedAt");
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

    public int getNextVersion(String stableId) {
        return dao.getNextVersion(stableId);
    }
}
