package bio.terra.pearl.core.service.notification.email;

import bio.terra.pearl.core.dao.notification.LocalizedEmailTemplateDao;
import bio.terra.pearl.core.model.notification.LocalizedEmailTemplate;
import bio.terra.pearl.core.service.ImmutableEntityService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class LocalizedEmailTemplateService extends ImmutableEntityService<LocalizedEmailTemplate, LocalizedEmailTemplateDao> {
    public LocalizedEmailTemplateService(LocalizedEmailTemplateDao dao) {
        super(dao);
    }

    public List<LocalizedEmailTemplate> findByEmailTemplate(UUID emailTemplateId) {
        return dao.findByEmailTemplate(emailTemplateId);
    }

    public Optional<LocalizedEmailTemplate> findByEmailTemplate(UUID emailTemplateId, String language) {
        return dao.findByEmailTemplate(emailTemplateId, language);
    }

}
