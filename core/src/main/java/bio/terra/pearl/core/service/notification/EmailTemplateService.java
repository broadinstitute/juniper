package bio.terra.pearl.core.service.notification;

import bio.terra.pearl.core.dao.notification.EmailTemplateDao;
import bio.terra.pearl.core.model.notification.EmailTemplate;
import bio.terra.pearl.core.service.CrudService;
import org.springframework.stereotype.Service;

@Service
public class EmailTemplateService extends CrudService<EmailTemplate, EmailTemplateDao> {
    public EmailTemplateService(EmailTemplateDao dao) {
        super(dao);
    }
}
