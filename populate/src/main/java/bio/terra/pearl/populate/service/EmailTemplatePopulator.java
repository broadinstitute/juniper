package bio.terra.pearl.populate.service;

import bio.terra.pearl.core.model.notification.EmailTemplate;
import bio.terra.pearl.core.model.notification.NotificationConfig;
import bio.terra.pearl.core.service.notification.EmailTemplateService;
import bio.terra.pearl.core.service.portal.PortalService;
import bio.terra.pearl.populate.dao.EmailTemplatePopulateDao;
import bio.terra.pearl.populate.dto.notifications.EmailTemplatePopDto;
import bio.terra.pearl.populate.dto.notifications.NotificationConfigPopDto;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class EmailTemplatePopulator extends Populator<EmailTemplate> {
    private EmailTemplateService emailTemplateService;
    private PortalService portalService;
    private EmailTemplatePopulateDao emailTemplatePopulateDao;

    public EmailTemplatePopulator(EmailTemplateService emailTemplateService,
                                  PortalService portalService,
                                  EmailTemplatePopulateDao emailTemplatePopulateDao) {
        this.emailTemplateService = emailTemplateService;
        this.portalService = portalService;
        this.emailTemplatePopulateDao = emailTemplatePopulateDao;
    }

    @Override
    public EmailTemplate populateFromString(String fileString, FilePopulateConfig config) throws IOException {
        EmailTemplatePopDto templatePopDto = objectMapper.readValue(fileString, EmailTemplatePopDto.class);
        String newContent = filePopulateService.readFile(templatePopDto.getBodyPopulateFile(), config);
        templatePopDto.setBody(newContent);
        UUID portalId = portalService.findOneByShortcode(config.getPortalShortcode()).get().getId();
        templatePopDto.setPortalId(portalId);
        Optional<EmailTemplate> existingOpt = fetchFromPopDto(templatePopDto);

        if (existingOpt.isPresent()) {
            EmailTemplate existing = existingOpt.get();
            // don't delete the template, since it may have other entities attached to it. Just mod the content
            existing.setBody(templatePopDto.getBody());
            existing.setSubject(templatePopDto.getSubject());
            existing.setName(templatePopDto.getName());
            emailTemplatePopulateDao.update(existing);
            return existing;
        }
        return emailTemplateService.create(templatePopDto);
    }

    public NotificationConfig convertNotificationConfig(NotificationConfigPopDto configPopDto) {
        NotificationConfig config = new NotificationConfig();
        BeanUtils.copyProperties(configPopDto, config);
        EmailTemplate template = emailTemplateService.findByStableId(configPopDto.getEmailTemplateStableId(),
                configPopDto.getEmailTemplateVersion()).get();
        config.setEmailTemplateId(template.getId());
        return config;
    }

    public Optional<EmailTemplate> fetchFromPopDto(EmailTemplatePopDto templatePopDto) {
        return emailTemplateService.findByStableId(templatePopDto.getStableId(), templatePopDto.getVersion());
    }
}
