package bio.terra.pearl.populate.service.extract;

import bio.terra.pearl.core.model.notification.EmailTemplate;
import bio.terra.pearl.core.model.notification.LocalizedEmailTemplate;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.service.notification.email.EmailTemplateService;
import bio.terra.pearl.core.service.notification.email.LocalizedEmailTemplateService;
import bio.terra.pearl.populate.dto.notifications.EmailTemplatePopDto;
import bio.terra.pearl.populate.dto.notifications.LocalizedEmailTemplatePopDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailTemplateExtractor {
    private final EmailTemplateService emailTemplateService;
    private final ObjectMapper objectMapper;

    public EmailTemplateExtractor(EmailTemplateService emailTemplateService, ObjectMapper objectMapper) {
        this.emailTemplateService = emailTemplateService;
        this.objectMapper = objectMapper;
    }

    public void writeEmailTemplates(Portal portal, ExtractPopulateContext context) {
        List<EmailTemplate> templates = emailTemplateService.findByPortalId(portal.getId());
        for (EmailTemplate template : templates) {
            emailTemplateService.attachLocalizedTemplates(template);
            writeEmailTemplate(template, context);
        }
    }

    public void writeEmailTemplate(EmailTemplate template, ExtractPopulateContext context) {
        EmailTemplatePopDto templatePopDto = new EmailTemplatePopDto();
        BeanUtils.copyProperties(template, templatePopDto, "id", "portalId", "localizedEmailTemplates");
        for (LocalizedEmailTemplate localizedTemplate : template.getLocalizedEmailTemplates()) {
            LocalizedEmailTemplatePopDto localizedTemplatePopDto = new LocalizedEmailTemplatePopDto();
            BeanUtils.copyProperties(localizedTemplate, localizedTemplatePopDto, "id", "emailTemplateId", "body");
            String fileNameForLocalizedTemplate = "emails/%s/%s".formatted(localizedTemplate.getLanguage(), fileNameForTemplate(template, false, false));
            try {
                context.writeFileForEntity(fileNameForLocalizedTemplate, localizedTemplate.getBody(), localizedTemplate.getId());
                localizedTemplatePopDto.setBodyPopulateFile(fileNameForLocalizedTemplate);
            } catch (Exception e) {
                throw new RuntimeException("Error writing localized email template %s-%s to html".formatted(template.getStableId(), template.getVersion()), e);
            }
            templatePopDto.getLocalizedEmailTemplateDtos().add(localizedTemplatePopDto);
        }

        String fileName = fileNameForTemplate(template, true, true);
        try {
            String fileString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(templatePopDto);
            context.writeFileForEntity(fileName, fileString, template.getId());
            context.getPortalPopDto().getEmailTemplateFiles().add(fileName);
        } catch (Exception e) {
            throw new RuntimeException("Error writing email template %s-%s to json".formatted(template.getStableId(), template.getVersion()), e);
        }

        context.getPortalPopDto().getEmailTemplateFiles().add(fileName);
    }

    protected String fileNameForTemplate(EmailTemplate template, boolean isJson, boolean includePath) {
        return "%s%s-%s.%s".formatted(
                includePath ? "emails/" : "",
                template.getStableId(),
                template.getVersion(),
                isJson ? "json" : "html");
    }
}
