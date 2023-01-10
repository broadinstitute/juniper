package bio.terra.pearl.core.service.survey;

import bio.terra.pearl.core.dao.survey.SurveyDao;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.service.CrudService;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SurveyService extends CrudService<Survey, SurveyDao> {

    public SurveyService(SurveyDao surveyDao) {
        super(surveyDao);
    }

    public Optional<Survey> findByStableId(String stableId, int version) {
        return dao.findByStableId(stableId, version);
    }

    @Transactional
    public void deleteByPortalId(UUID portalId) {
        List<Survey> surveys = dao.findByPortalId(portalId);
        for (Survey survey : surveys) {
            delete(survey.getId(), new HashSet<>());
        }
    }

    /** create a new version of the given survey with updated content.  the version will be the next
     * available number for the given stableId */
    @Transactional
    public Survey createNewVersion(AdminUser user, UUID portalId, Survey survey) {
        // TODO check user permissions
        Survey newSurvey = new Survey();
        BeanUtils.copyProperties(survey, newSurvey, "id", "version", "createdAt", "lastUpdatedAt");
        newSurvey.setPortalId(portalId);
        int nextVersion = dao.getNextVersion(survey.getStableId());
        newSurvey.setVersion(nextVersion);
        return create(newSurvey);
    }
}
