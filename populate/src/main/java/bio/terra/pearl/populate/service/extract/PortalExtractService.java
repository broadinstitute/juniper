package bio.terra.pearl.populate.service.extract;

import bio.terra.pearl.core.model.notification.Trigger;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.study.PortalStudy;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.portal.PortalEnvironmentConfigService;
import bio.terra.pearl.core.service.portal.PortalEnvironmentService;
import bio.terra.pearl.core.service.portal.PortalService;
import bio.terra.pearl.core.service.portal.exception.PortalConfigMissing;
import bio.terra.pearl.populate.dto.PortalEnvironmentPopDto;
import bio.terra.pearl.populate.dto.survey.SurveyPopDto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.ZipOutputStream;

/** Exports all configurations for a portal */
@Service
public class PortalExtractService {
    private final PortalService portalService;
    private final PortalEnvironmentService portalEnvironmentService;
    private final PortalEnvironmentConfigService portalEnvironmentConfigService;
    private final SurveyExtractor surveyExtractor;
    private final SiteContentExtractor siteContentExtractor;
    private final StudyExtractor studyExtractor;
    private final ImageExtractor imageExtractor;
    private final ConsentFormExtractor consentFormExtractor;
    private final EmailTemplateExtractor emailTemplateExtractor;
    private final ObjectMapper objectMapper;

    public PortalExtractService(PortalService portalService,
                                PortalEnvironmentService portalEnvironmentService,
                                PortalEnvironmentConfigService portalEnvironmentConfigService,
                                SurveyExtractor surveyExtractor,
                                SiteContentExtractor siteContentExtractor,
                                StudyExtractor studyExtractor,
                                ImageExtractor imageExtractor,
                                ConsentFormExtractor consentFormExtractor,
                                EmailTemplateExtractor emailTemplateExtractor,
                                @Qualifier("extractionObjectMapper") ObjectMapper objectMapper) {
        this.portalService = portalService;
        this.portalEnvironmentService = portalEnvironmentService;
        this.portalEnvironmentConfigService = portalEnvironmentConfigService;
        this.surveyExtractor = surveyExtractor;
        this.siteContentExtractor = siteContentExtractor;
        this.studyExtractor = studyExtractor;
        this.imageExtractor = imageExtractor;
        this.consentFormExtractor = consentFormExtractor;
        this.emailTemplateExtractor = emailTemplateExtractor;
        this.objectMapper = objectMapper;
        this.objectMapper.addMixIn(Portal.class, PortalMixin.class);
    }

    public void extract(String portalShortcode, OutputStream os) throws IOException {
        Portal portal = portalService.findOneByShortcode(portalShortcode)
                .orElseThrow(() -> new NotFoundException("Portal not found: " + portalShortcode));
        ZipOutputStream zipOut = new ZipOutputStream(os);
        ExtractPopulateContext context = new ExtractPopulateContext(portal, zipOut);
        imageExtractor.writeImages(portal, context);
        siteContentExtractor.writeSiteContents(portal, context);
        consentFormExtractor.writeForms(portal, context);
        surveyExtractor.writeSurveys(portal, context);
        emailTemplateExtractor.writeEmailTemplates(portal, context);
        studyExtractor.writeStudies(portal, context);
        extractPortalEnvs(portal, context);
        writePortal(portal, context);
        zipOut.finish();
    }

    /** this should be called last, as it relies on the portalPopDto in the context being fully populated */
    public void writePortal(Portal portal, ExtractPopulateContext context) {
        try {
            String fileString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(context.getPortalPopDto());
            context.writeFileForEntity("portal.json", fileString, portal.getId());
        } catch (Exception e) {
            throw new RuntimeException("Error writing portal to json", e);
        }
    }

    /** updates the portalPopDto in the context with the portal environment configs */
    public void extractPortalEnvs(Portal portal, ExtractPopulateContext context) {
        portalEnvironmentService.findByPortal(portal.getId()).forEach(env -> {
            PortalEnvironmentPopDto envPopDto = new PortalEnvironmentPopDto();
            envPopDto.setEnvironmentName(env.getEnvironmentName());
            envPopDto.setPortalEnvironmentConfig(portalEnvironmentConfigService
                    .find(env.getPortalEnvironmentConfigId()).orElseThrow(PortalConfigMissing::new));
            if (env.getSiteContentId() != null) {
                SiteContentExtractor.SiteContentPopDtoStub siteContentPopDto = new SiteContentExtractor.SiteContentPopDtoStub();
                siteContentPopDto.setPopulateFileName(context.getFileNameForEntity(env.getSiteContentId()));
                envPopDto.setSiteContentPopDto(siteContentPopDto);
            }
            if (env.getPreRegSurveyId() != null) {
                SurveyPopDto surveyPopDto = new SurveyPopDto();
                surveyPopDto.setPopulateFileName(context.getFileNameForEntity(env.getPreRegSurveyId()));
                envPopDto.setPreRegSurveyDto(surveyPopDto);
            }
            context.getPortalPopDto().getPortalEnvironmentDtos().add(envPopDto);
        });
    }

    protected static class PortalMixin {
        @JsonIgnore public List<PortalParticipantUser> getPortalParticipantUsers() { return null; }
        @JsonIgnore public List<PortalStudy> getPortalStudies() { return null; }
        @JsonIgnore public List<PortalEnvironment> getPortalEnvironments() { return null; }
    }

    protected static class PortalEnvironmentMixin {
        @JsonIgnore public List<Trigger> getNotificationConfigs() { return null; }
    }
}
