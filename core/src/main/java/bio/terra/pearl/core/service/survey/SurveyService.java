package bio.terra.pearl.core.service.survey;

import bio.terra.pearl.core.dao.survey.AnswerMappingDao;
import bio.terra.pearl.core.dao.survey.SurveyDao;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.survey.AnswerMapping;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.CrudService;
import java.util.*;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SurveyService extends CrudService<Survey, SurveyDao> {
    private AnswerMappingDao answerMappingDao;

    public SurveyService(SurveyDao surveyDao, AnswerMappingDao answerMappingDao) {
        super(surveyDao);
        this.answerMappingDao = answerMappingDao;
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
        return savedSurvey;
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
}
