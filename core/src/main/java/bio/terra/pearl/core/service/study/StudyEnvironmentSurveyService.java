package bio.terra.pearl.core.service.study;

import bio.terra.pearl.core.dao.study.StudyEnvironmentSurveyDao;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.survey.StudyEnvironmentSurvey;
import bio.terra.pearl.core.service.CrudService;
import org.springframework.stereotype.Service;

@Service
public class StudyEnvironmentSurveyService extends CrudService<StudyEnvironmentSurvey, StudyEnvironmentSurveyDao> {
    public StudyEnvironmentSurveyService(StudyEnvironmentSurveyDao dao) {
        super(dao);
    }

    public StudyEnvironmentSurvey update(AdminUser user, StudyEnvironmentSurvey survey) {
        return dao.update(survey);
    }
}
