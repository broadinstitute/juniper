package bio.terra.pearl.populate.service.extract;

import bio.terra.pearl.core.model.consent.ConsentForm;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.service.consent.ConsentFormService;
import bio.terra.pearl.populate.dto.consent.ConsentFormPopDto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConsentFormExtractor {
    private final ConsentFormService consentFormService;
    private final ObjectMapper objectMapper;

    public ConsentFormExtractor(ConsentFormService consentFormService, @Qualifier("extractionObjectMapper") ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.consentFormService = consentFormService;
    }

    /** writes all versions of all surveys to the zip file */
    public void writeForms(Portal portal, ExtractPopulateContext context) {
        List<ConsentForm> forms = consentFormService.findByPortalId(portal.getId());
        for (ConsentForm form : forms) {
            writeForm(form, context);
        }
    }

    public void writeForm(ConsentForm form, ExtractPopulateContext context) {
        ConsentFormPopDto formPopDto = new ConsentFormPopDto();
        BeanUtils.copyProperties(form, formPopDto, "id", "portalId", "content");
        try {
            formPopDto.setJsonContent(objectMapper.readTree(form.getContent()));
            String fileString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(formPopDto);
            context.writeFileForEntity(fileNameForForm(form), fileString, form.getId());
            context.getPortalPopDto().getConsentFormFiles().add(fileNameForForm(form));
        } catch (Exception e) {
            throw new RuntimeException("Error writing consent %s-%s to json".formatted(form.getStableId(), form.getVersion()), e);
        }
    }

    protected String fileNameForForm(ConsentForm form) {
        return "surveys/%s-%s.json".formatted(form.getStableId(), form.getVersion());
    }

    /** stub class for just writing out the file name */
    protected static class ConsentFormPopDtoStub extends ConsentFormPopDto {
        @JsonIgnore @Override
        public int getVersion() { return 0; }
    }
}
