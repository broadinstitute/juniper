package bio.terra.pearl.populate.service.export;

import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.portal.PortalService;
import bio.terra.pearl.core.service.study.*;
import bio.terra.pearl.core.service.survey.SurveyService;
import bio.terra.pearl.populate.dto.survey.SurveyPopDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.zip.ZipOutputStream;

/** Exports all configurations for a portal */
@Service
public class PortalExportService {
    private final PortalService portalService;
    private final SurveyService surveyService;
    private final ObjectMapper objectMapper;

    public PortalExportService(PortalService portalService,
                               SurveyService surveyService, ObjectMapper objectMapper) {
        this.portalService = portalService;
        this.surveyService = surveyService;
        this.objectMapper = objectMapper;
    }

    public void exportToFile(String portalShortcode) throws IOException {
        File zipFile = new File("portal-%s-%s.zip".formatted(portalShortcode,
                ZonedDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)));
        export(portalShortcode, new FileOutputStream(zipFile));
    }

    public void export(String portalShortcode, OutputStream os) throws IOException {
        Portal portal = portalService.findOneByShortcode(portalShortcode)
                .orElseThrow(() -> new NotFoundException("Portal not found: " + portalShortcode));
        ZipOutputStream zipOut = new ZipOutputStream(os);
        ExportPopulateContext context = new ExportPopulateContext(portal, zipOut);
        writeSurveys(portal, context);
        writePortal(context);
        zipOut.finish();
    }

    /** writes all versions of all surveys to the zip file */
    public void writeSurveys(Portal portal, ExportPopulateContext context) {
        List<Survey> surveys = surveyService.findByPortalId(portal.getId());
        for (Survey survey : surveys) {
            surveyService.attachAnswerMappings(survey);
            writeSurvey(survey, context);
        }
    }

    /** this should be called last, as it relies on the portalPopDto in the context being fully populated */
    public void writePortal(ExportPopulateContext context) {
        try {
            String fileString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(context.getPortalPopDto());
            context.writeFileForEntity("portal.json", fileString, context.getPortalPopDto().getId());
        } catch (Exception e) {
            throw new RuntimeException("Error writing portal to json", e);
        }
    }

    public void writeSurvey(Survey survey, ExportPopulateContext context) {
        SurveyPopDto surveyPopDto = new SurveyPopDto();
        BeanUtils.copyProperties(survey, surveyPopDto, "id", "portalId", "content");
        try {
            surveyPopDto.setJsonContent(objectMapper.readTree(survey.getContent()));
            String fileString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(surveyPopDto);
            context.writeFileForEntity(fileNameForSurvey(survey), fileString, survey.getId());
            context.getPortalPopDto().getSurveyFiles().add(fileNameForSurvey(survey));
        } catch (Exception e) {
            throw new RuntimeException("Error writing survey %s-%s to json".formatted(survey.getStableId(), survey.getVersion()), e);
        }
    }

    protected String fileNameForSurvey(Survey survey) {
        return "surveys/%s-%s.json".formatted(survey.getStableId(), survey.getVersion());
    }

}
