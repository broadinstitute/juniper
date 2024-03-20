package bio.terra.pearl.core.dao.notification;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.model.notification.LocalizedEmailTemplate;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class LocalizedEmailTemplateDao extends BaseJdbiDao<LocalizedEmailTemplate> {

    public LocalizedEmailTemplateDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<LocalizedEmailTemplate> getClazz() {
        return LocalizedEmailTemplate.class;
    }

    public List<LocalizedEmailTemplate> findByEmailTemplate(UUID emailTemplateId) {
        return findAllByProperty("email_template_id", emailTemplateId);
    }

    public Optional<LocalizedEmailTemplate> findByEmailTemplate(UUID emailTemplateId, String language) {
        return findByTwoProperties("email_template_id", emailTemplateId, "language", language);
    }

}
