package bio.terra.pearl.core.dao.notification;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.model.notification.EmailTemplate;
import java.util.Optional;
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

    public Optional<EmailTemplate> findByStableId(String stableId, int version) {
        return findByTwoProperties("stable_id", stableId, "version", version);
    }
}
