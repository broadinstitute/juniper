package bio.terra.pearl.populate.service.extract;

import bio.terra.pearl.core.dao.dashboard.ParticipantDashboardAlertDao;
import bio.terra.pearl.core.model.notification.Trigger;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.portal.PortalEnvironmentLanguage;
import bio.terra.pearl.core.model.study.PortalStudy;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.portal.PortalEnvironmentConfigService;
import bio.terra.pearl.core.service.portal.PortalEnvironmentService;
import bio.terra.pearl.core.service.portal.PortalLanguageService;
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
    private final MediaExtractor mediaExtractor;
    private final ConsentFormExtractor consentFormExtractor;
    private final EmailTemplateExtractor emailTemplateExtractor;
    private final ParticipantDashboardAlertDao participantDashboardAlertDao;
    private final PortalLanguageService portalLanguageService;

    private final ObjectMapper objectMapper;


    public PortalExtractService(PortalService portalService,
                                PortalEnvironmentService portalEnvironmentService,
                                PortalEnvironmentConfigService portalEnvironmentConfigService,
                                SurveyExtractor surveyExtractor,
                                SiteContentExtractor siteContentExtractor,
                                StudyExtractor studyExtractor,
                                MediaExtractor mediaExtractor,
                                ConsentFormExtractor consentFormExtractor,
                                EmailTemplateExtractor emailTemplateExtractor,
                                ParticipantDashboardAlertDao participantDashboardAlertDao,
                                PortalLanguageService portalLanguageService,
                                @Qualifier("extractionObjectMapper") ObjectMapper objectMapper) {
        this.portalService = portalService;
        this.portalEnvironmentService = portalEnvironmentService;
        this.portalEnvironmentConfigService = portalEnvironmentConfigService;
        this.surveyExtractor = surveyExtractor;
        this.siteContentExtractor = siteContentExtractor;
        this.studyExtractor = studyExtractor;
        this.mediaExtractor = mediaExtractor;
        this.consentFormExtractor = consentFormExtractor;
        this.emailTemplateExtractor = emailTemplateExtractor;
        this.participantDashboardAlertDao = participantDashboardAlertDao;
        this.portalLanguageService = portalLanguageService;
        this.objectMapper = objectMapper;
        this.objectMapper.addMixIn(Portal.class, PortalMixin.class);
    }

    public void extract(String portalShortcode, OutputStream os) throws IOException {
        Portal portal = portalService.findOneByShortcode(portalShortcode)
                .orElseThrow(() -> new NotFoundException("Portal not found: " + portalShortcode));
        ZipOutputStream zipOut = new ZipOutputStream(os);
        ExtractPopulateContext context = new ExtractPopulateContext(portal, zipOut);
        mediaExtractor.writeMedia(portal, context);
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
        List<PortalEnvironment> portalEnvironments = portalEnvironmentService.findByPortal(portal.getId());
        for (PortalEnvironment portalEnvironment : portalEnvironments) {
            PortalEnvironmentPopDto popDto = extractPortalEnv(portalEnvironment, context);
            context.getPortalPopDto().getPortalEnvironmentDtos().add(popDto);
        }
    }

    public PortalEnvironmentPopDto extractPortalEnv(PortalEnvironment portalEnv, ExtractPopulateContext context) {
        PortalEnvironmentPopDto envPopDto = new PortalEnvironmentPopDto();
        envPopDto.setEnvironmentName(portalEnv.getEnvironmentName());
        envPopDto.setPortalEnvironmentConfig(portalEnvironmentConfigService
                .find(portalEnv.getPortalEnvironmentConfigId()).orElseThrow(PortalConfigMissing::new));
        if (portalEnv.getSiteContentId() != null) {
            SiteContentExtractor.SiteContentPopDtoStub siteContentPopDto = new SiteContentExtractor.SiteContentPopDtoStub();
            siteContentPopDto.setPopulateFileName(context.getFileNameForEntity(portalEnv.getSiteContentId()));
            envPopDto.setSiteContentPopDto(siteContentPopDto);
        }
        if (portalEnv.getPreRegSurveyId() != null) {
            SurveyPopDto surveyPopDto = new SurveyPopDto();
            surveyPopDto.setPopulateFileName(context.getFileNameForEntity(portalEnv.getPreRegSurveyId()));
            envPopDto.setPreRegSurveyDto(surveyPopDto);
        }
        envPopDto.setSupportedLanguages(portalLanguageService.findByPortalEnvId(portalEnv.getId()));
        envPopDto.setParticipantDashboardAlerts(participantDashboardAlertDao.findByPortalEnvironmentId(portalEnv.getId()));
        return envPopDto;
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
