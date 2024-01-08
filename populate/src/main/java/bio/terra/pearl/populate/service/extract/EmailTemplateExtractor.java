package bio.terra.pearl.populate.service.extract;

import bio.terra.pearl.core.model.notification.EmailTemplate;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.service.notification.email.EmailTemplateService;
import bio.terra.pearl.populate.dto.notifications.EmailTemplatePopDto;
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
            writeEmailTemplate(template, context);
        }
    }

    public void writeEmailTemplate(EmailTemplate template, ExtractPopulateContext context) {
        EmailTemplatePopDto templatePopDto = new EmailTemplatePopDto();
        BeanUtils.copyProperties(template, templatePopDto, "id", "portalId", "body");
        try {
            // we need to write both the json file specifying the properties and the html file specifying the content
            List<String> fileNames = List.of(
                fileNameForTemplate(template, true, true),
                fileNameForTemplate(template, false, true)
            );
            // the path to the html will be relative to the .json file, so omits the "emails/" directory prefix
            templatePopDto.setBodyPopulateFile(fileNameForTemplate(template, false, false));
            List<String> fileContents = List.of(
                objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(templatePopDto),
                template.getBody()
            );
            context.writeFilesForEntity(fileNames, fileContents, template.getId());
            context.getPortalPopDto().getEmailTemplateFiles().add(fileNames.get(0));
        } catch (Exception e) {
            throw new RuntimeException("Error writing email template %s-%s to json".formatted(template.getStableId(), template.getVersion()), e);
        }
    }

    protected String fileNameForTemplate(EmailTemplate template, boolean isJson, boolean includePath) {
        return "%s%s-%s.%s".formatted(
                includePath ? "emails/" : "",
                template.getStableId(),
                template.getVersion(),
                isJson ? "json" : "html");
    }
}
