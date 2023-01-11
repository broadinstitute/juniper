package bio.terra.pearl.core.service.study;

import bio.terra.pearl.core.dao.study.StudyEnvironmentConsentDao;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.consent.StudyEnvironmentConsent;
import bio.terra.pearl.core.service.CrudService;
import org.springframework.stereotype.Service;

@Service
public class StudyEnvironmentConsentService extends CrudService<StudyEnvironmentConsent, StudyEnvironmentConsentDao> {
    public StudyEnvironmentConsentService(StudyEnvironmentConsentDao dao) {
        super(dao);
    }
    public StudyEnvironmentConsent update(AdminUser user, StudyEnvironmentConsent configuredConsent) {
        return dao.update(configuredConsent);
    }
}
