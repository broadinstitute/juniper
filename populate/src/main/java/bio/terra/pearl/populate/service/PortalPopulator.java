package bio.terra.pearl.populate.service;

import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.dashboard.ParticipantDashboardAlert;
import bio.terra.pearl.core.model.portal.MailingListContact;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.portal.PortalEnvironmentLanguage;
import bio.terra.pearl.core.model.site.SiteContent;
import bio.terra.pearl.core.model.study.PortalStudy;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.portal.MailingListContactService;
import bio.terra.pearl.core.service.portal.PortalDashboardConfigService;
import bio.terra.pearl.core.service.portal.PortalEnvironmentService;
import bio.terra.pearl.core.service.portal.PortalEnvironmentLanguageService;
import bio.terra.pearl.core.service.portal.PortalService;
import bio.terra.pearl.core.service.publishing.PortalEnvironmentChangeRecordService;
import bio.terra.pearl.core.service.study.PortalStudyService;
import bio.terra.pearl.populate.dto.AdminUserPopDto;
import bio.terra.pearl.populate.dto.PortalEnvironmentChangeRecordPopDto;
import bio.terra.pearl.populate.dto.PortalEnvironmentPopDto;
import bio.terra.pearl.populate.dto.PortalPopDto;
import bio.terra.pearl.populate.dto.site.SiteMediaPopDto;
import bio.terra.pearl.populate.service.contexts.FilePopulateContext;
import bio.terra.pearl.populate.service.contexts.PortalPopulateContext;
import bio.terra.pearl.populate.util.ZipUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.zip.ZipInputStream;

@Service
public class PortalPopulator extends BasePopulator<Portal, PortalPopDto, FilePopulateContext> {
    private final PortalService portalService;
    private final PortalEnvironmentService portalEnvironmentService;
    private final SiteMediaPopulator siteMediaPopulator;
    private final StudyPopulator studyPopulator;
    private final SurveyPopulator surveyPopulator;
    private final SiteContentPopulator siteContentPopulator;
    private final PortalStudyService portalStudyService;
    private final PortalParticipantUserPopulator portalParticipantUserPopulator;
    private final MailingListContactService mailingListContactService;
    private final AdminUserPopulator adminUserPopulator;
    private final EmailTemplatePopulator emailTemplatePopulator;
    private final PortalDashboardConfigService portalDashboardConfigService;
    private final PortalEnvironmentLanguageService portalEnvironmentLanguageService;
    private final PortalEnvironmentChangeRecordPopulator portalEnvironmentChangeRecordPopulator;


    public PortalPopulator(PortalService portalService,
                           StudyPopulator studyPopulator,
                           PortalStudyService portalStudyService,
                           SiteContentPopulator siteContentPopulator,
                           PortalParticipantUserPopulator portalParticipantUserPopulator,
                           PortalEnvironmentService portalEnvironmentService,
                           PortalDashboardConfigService portalDashboardConfigService,
                           SiteMediaPopulator siteMediaPopulator, SurveyPopulator surveyPopulator,
                           AdminUserPopulator adminUserPopulator,
                           MailingListContactService mailingListContactService,
                           EmailTemplatePopulator emailTemplatePopulator,
                           PortalEnvironmentLanguageService portalEnvironmentLanguageService,
                           PortalEnvironmentChangeRecordPopulator portalEnvironmentChangeRecordPopulator) {
        this.siteContentPopulator = siteContentPopulator;
        this.portalParticipantUserPopulator = portalParticipantUserPopulator;
        this.portalEnvironmentService = portalEnvironmentService;
        this.portalDashboardConfigService = portalDashboardConfigService;
        this.siteMediaPopulator = siteMediaPopulator;
        this.surveyPopulator = surveyPopulator;
        this.portalService = portalService;
        this.studyPopulator = studyPopulator;
        this.portalStudyService = portalStudyService;
        this.mailingListContactService = mailingListContactService;
        this.adminUserPopulator = adminUserPopulator;
        this.emailTemplatePopulator = emailTemplatePopulator;
        this.portalEnvironmentLanguageService = portalEnvironmentLanguageService;
        this.portalEnvironmentChangeRecordPopulator = portalEnvironmentChangeRecordPopulator;
    }

    private void populateStudy(String studyFileName, PortalPopulateContext context, Portal portal, boolean overwrite) {
        Study newStudy = studyPopulator.populate(context.newFrom(studyFileName), overwrite);
        Optional<PortalStudy> portalStudyOpt = portalStudyService.findStudyInPortal(newStudy.getShortcode(), portal.getId());
        if (portalStudyOpt.isEmpty()) {
            PortalStudy portalStudy = portalStudyService.create(portal.getId(), newStudy.getId());
            portal.getPortalStudies().add(portalStudy);
            portalStudy.setStudy(newStudy);
        }
    }

    private void initializePortalEnv(PortalEnvironmentPopDto portalEnvPopDto,
                                     PortalPopulateContext portalPopContext, boolean overwrite) {
        /** unless we've overwritten the whole portal (overwirte mode) don't alter non-sandbox environments */
        if (!portalEnvPopDto.getEnvironmentName().equals(EnvironmentName.sandbox) && !overwrite) {
            return;
        }

        PortalPopulateContext envConfig = portalPopContext.newFrom(portalEnvPopDto.getEnvironmentName());
        // we're iterating over each population file spec, so now match the current on to the
        // actual entity that got saved as a result of the portal create call.
        PortalEnvironment savedEnv = portalEnvironmentService
                .findOne(portalPopContext.getPortalShortcode(), portalEnvPopDto.getEnvironmentName()).get();

        if (portalEnvPopDto.getSiteContentPopDto() != null) {
            SiteContent content = siteContentPopulator
                    .findFromDto(portalEnvPopDto.getSiteContentPopDto(), portalPopContext).get();
            savedEnv.setSiteContent(content);
            savedEnv.setSiteContentId(content.getId());
        }
        if (portalEnvPopDto.getPreRegSurveyDto() != null) {
            Survey matchedSurvey = surveyPopulator.findFromDto(portalEnvPopDto.getPreRegSurveyDto(), portalPopContext).get();
            savedEnv.setPreRegSurveyId(matchedSurvey.getId());
        }
        for (String userFileName : portalEnvPopDto.getParticipantUserFiles()) {
            portalParticipantUserPopulator.populate(envConfig.newFrom(userFileName), overwrite);
        }
        for (ParticipantDashboardAlert alert : portalEnvPopDto.getParticipantDashboardAlerts()) {
            alert.setPortalEnvironmentId(savedEnv.getId());
            portalDashboardConfigService.create(alert);
        }
        for (PortalEnvironmentLanguage language : portalEnvPopDto.getSupportedLanguages()) {
            language.setPortalEnvironmentId(savedEnv.getId());
            portalEnvironmentLanguageService.create(language);
        }
        // re-save the portal environment to update it with any attached siteContents or preRegSurveys
        portalEnvironmentService.update(savedEnv);
        populateMailingList(portalEnvPopDto, savedEnv, overwrite);
    }

    private void populateMailingList(PortalEnvironmentPopDto portalEnvPopDto, PortalEnvironment savedEnv,
                                     boolean overwrite) {
        if (!overwrite) {
            // we don't support updating mailing lists in-place yet
            return;
        }
        DataAuditInfo auditInfo = DataAuditInfo.builder().systemProcess(
                DataAuditInfo.systemProcessName(getClass(), "populateMailingList")).build();
        for (MailingListContact contact : portalEnvPopDto.getMailingListContacts()) {
            contact.setPortalEnvironmentId(savedEnv.getId());
            contact.setEmail(contact.getEmail());
            contact.setName(contact.getName());
            mailingListContactService.create(contact, auditInfo);
        }
    }

    @Override
    protected Class<PortalPopDto> getDtoClazz() {
        return PortalPopDto.class;
    }

    @Override
    public void preProcessDto(PortalPopDto popDto, FilePopulateContext context) {
        if (context.getShortcodeOverride() != null) {
            popDto.setShortcode(context.getShortcodeOverride());
            popDto.setName(context.getShortcodeOverride());
        }
    }

    @Override
    public Optional<Portal> findFromDto(PortalPopDto popDto, FilePopulateContext context) {
        return portalService.findOneByShortcode(popDto.getShortcode());
    }

    @Override
    public Portal overwriteExisting(Portal existingObj, PortalPopDto popDto, FilePopulateContext context) throws IOException {
        Set<CascadeProperty> set = new HashSet<>();
        set.add(PortalService.AllowedCascades.STUDY);
        set.add(PortalService.AllowedCascades.PARTICIPANT_USER);
        portalService.delete(existingObj.getId(), set);
        return createNew(popDto, context, true);
    }

    @Override
    public Portal createPreserveExisting(Portal existingObj, PortalPopDto popDto, FilePopulateContext context) throws IOException {
        existingObj.setName(popDto.getName());
        portalService.update(existingObj);
        return populateChildren(existingObj, popDto, context, false);
    }

    @Override
    public Portal createNew(PortalPopDto popDto, FilePopulateContext context, boolean overwrite) throws IOException {
        Portal portal = portalService.create(popDto);
        return populateChildren(portal, popDto, context, overwrite);
    }

    protected Portal populateChildren(Portal portal, PortalPopDto popDto, FilePopulateContext context, boolean overwrite) throws IOException {
        PortalPopulateContext portalPopContext = new PortalPopulateContext(context, portal.getShortcode(), null);

        for (AdminUserPopDto adminUserPopDto : popDto.getAdminUsers()) {
            adminUserPopulator.populateForPortal(adminUserPopDto, portalPopContext, overwrite, portal);
        }
        for (SiteMediaPopDto imagePopDto : popDto.getSiteMediaDtos()) {
            siteMediaPopulator.populateFromDto(imagePopDto, portalPopContext, overwrite);
        }
        for (String surveyFile : popDto.getSurveyFiles()) {
            surveyPopulator.populate(portalPopContext.newFrom(surveyFile), overwrite);
        }
        for (String emailTemplateFile : popDto.getEmailTemplateFiles()) {
            emailTemplatePopulator.populate(portalPopContext.newFrom(emailTemplateFile), overwrite);
        }
        for (String siteContentFile : popDto.getSiteContentFiles()) {
            siteContentPopulator.populate(portalPopContext.newFrom(siteContentFile), overwrite);
        }

        for (PortalEnvironmentPopDto portalEnvironment : popDto.getPortalEnvironmentDtos()) {
            initializePortalEnv(portalEnvironment, portalPopContext, overwrite);
        }
        for (String studyFileName : popDto.getPopulateStudyFiles()) {
            populateStudy(studyFileName, portalPopContext, portal, overwrite);
        }

        for (PortalEnvironmentChangeRecordPopDto changeRecordPopDto : popDto.getPortalEnvironmentChangeRecordPopDtos()) {
            portalEnvironmentChangeRecordPopulator.populateFromDto(changeRecordPopDto, portalPopContext, overwrite);
        }

        return portal;
    }

    /**
     * just populates the images from the given portal.json file.
     * Useful for populating/updating site content, which may rely on images form the root file
     * */
    public void populateImages(String portalFilePath, boolean overwrite) throws IOException {
        FilePopulateContext fileContext = new FilePopulateContext(portalFilePath);
        String portalFileString = filePopulateService.readFile(fileContext.getRootFileName(), fileContext);
        PortalPopDto popDto = readValue(portalFileString);
        PortalPopulateContext portalPopContext = new PortalPopulateContext(fileContext, popDto.getShortcode(), null);
        for (SiteMediaPopDto imagePopDto : popDto.getSiteMediaDtos()) {
            siteMediaPopulator.populateFromDto(imagePopDto, portalPopContext, overwrite);
        }
    }

    public Portal populateFromZipFile(ZipInputStream zipInputStream, boolean overwrite, String shortcodeOverride) throws IOException {
        String folderName =
                "portal_%s_%s"
                        .formatted(
                                ZonedDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE),
                                RandomStringUtils.randomAlphabetic(5));
        String tempDirName = FilePopulateService.TMP_POPULATE_DIR + "/" + folderName;
        File tempDir = new File(tempDirName);
        tempDir.mkdirs();
        ZipUtils.unzipFile(tempDir, zipInputStream);
        return populate(
                new FilePopulateContext(folderName + "/portal.json", true, shortcodeOverride), overwrite);
    }
}
