package bio.terra.pearl.populate.service;

import bio.terra.pearl.core.model.notification.EmailTemplate;
import bio.terra.pearl.core.model.notification.NotificationConfig;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.service.notification.EmailTemplateService;
import bio.terra.pearl.core.service.portal.PortalEnvironmentService;
import bio.terra.pearl.core.service.portal.PortalService;
import bio.terra.pearl.populate.dao.EmailTemplatePopulateDao;
import bio.terra.pearl.populate.dto.notifications.EmailTemplatePopDto;
import bio.terra.pearl.populate.dto.notifications.NotificationConfigPopDto;
import bio.terra.pearl.populate.service.contexts.PortalPopulateContext;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class EmailTemplatePopulator extends Populator<EmailTemplate, PortalPopulateContext> {
    private EmailTemplateService emailTemplateService;
    private PortalService portalService;
    private EmailTemplatePopulateDao emailTemplatePopulateDao;
    private PortalEnvironmentService portalEnvironmentService;

    public EmailTemplatePopulator(EmailTemplateService emailTemplateService,
                                  PortalService portalService,
                                  EmailTemplatePopulateDao emailTemplatePopulateDao,
                                  PortalEnvironmentService portalEnvironmentService) {
        this.emailTemplateService = emailTemplateService;
        this.portalService = portalService;
        this.emailTemplatePopulateDao = emailTemplatePopulateDao;
        this.portalEnvironmentService = portalEnvironmentService;
    }

    @Override
    public EmailTemplate populateFromString(String fileString, PortalPopulateContext context) throws IOException {
        EmailTemplatePopDto templatePopDto = objectMapper.readValue(fileString, EmailTemplatePopDto.class);
        String newContent = filePopulateService.readFile(templatePopDto.getBodyPopulateFile(), context);
        templatePopDto.setBody(newContent);
        UUID portalId = portalService.findOneByShortcode(context.getPortalShortcode()).get().getId();
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

    public NotificationConfig convertNotificationConfig(NotificationConfigPopDto configPopDto, PortalPopulateContext context) {
        NotificationConfig config = new NotificationConfig();
        BeanUtils.copyProperties(configPopDto, config);
        PortalEnvironment portalEnv = portalEnvironmentService
                .findOne(context.getPortalShortcode(), context.getEnvironmentName()).get();
        config.setPortalEnvironmentId(portalEnv.getId());
        EmailTemplate template = emailTemplateService.findByStableId(configPopDto.getEmailTemplateStableId(),
                configPopDto.getEmailTemplateVersion()).get();
        config.setEmailTemplateId(template.getId());
        return config;
    }

    public Optional<EmailTemplate> fetchFromPopDto(EmailTemplatePopDto templatePopDto) {
        return emailTemplateService.findByStableId(templatePopDto.getStableId(), templatePopDto.getVersion());
    }
}
