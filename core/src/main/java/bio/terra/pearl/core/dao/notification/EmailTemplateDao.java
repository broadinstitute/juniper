package bio.terra.pearl.core.dao.notification;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.model.notification.EmailTemplate;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

@Component
public class EmailTemplateDao extends BaseJdbiDao<EmailTemplate> {
    public EmailTemplateDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<EmailTemplate> getClazz() {
        return EmailTemplate.class;
    }
}
