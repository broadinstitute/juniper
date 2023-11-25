package bio.terra.pearl.populate.service.extract;

import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.portal.PortalService;
import bio.terra.pearl.core.service.survey.SurveyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.bind.Name;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipOutputStream;

/** Exports all configurations for a portal */
@Service
public class PortalExtractService {
    private final PortalService portalService;
    private final SurveyExtractor surveyExtractor;
    private final SiteContentExtractor siteContentExtractor;
    private final ObjectMapper objectMapper;

    public PortalExtractService(PortalService portalService,
                                SurveyExtractor surveyExtractor,
                                SiteContentExtractor siteContentExtractor, @Qualifier("extractionObjectMapper") ObjectMapper objectMapper) {
        this.portalService = portalService;
        this.surveyExtractor = surveyExtractor;
        this.siteContentExtractor = siteContentExtractor;
        this.objectMapper = objectMapper;
    }

    public void extract(String portalShortcode, OutputStream os) throws IOException {
        Portal portal = portalService.findOneByShortcode(portalShortcode)
                .orElseThrow(() -> new NotFoundException("Portal not found: " + portalShortcode));
        ZipOutputStream zipOut = new ZipOutputStream(os);
        ExportPopulateContext context = new ExportPopulateContext(portal, zipOut);
        siteContentExtractor.writeSiteContents(portal, context);
        surveyExtractor.writeSurveys(portal, context);
        writePortal(portal, context);
        zipOut.finish();
    }

    /** this should be called last, as it relies on the portalPopDto in the context being fully populated */
    public void writePortal(Portal portal, ExportPopulateContext context) {
        try {
            String fileString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(context.getPortalPopDto());
            context.writeFileForEntity("portal.json", fileString, portal.getId());
        } catch (Exception e) {
            throw new RuntimeException("Error writing portal to json", e);
        }
    }



}
