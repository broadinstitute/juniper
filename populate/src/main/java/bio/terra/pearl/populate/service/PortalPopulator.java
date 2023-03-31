package bio.terra.pearl.populate.service;

import bio.terra.pearl.core.model.portal.MailingListContact;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.site.SiteContent;
import bio.terra.pearl.core.model.study.PortalStudy;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.service.participant.PortalParticipantUserService;
import bio.terra.pearl.core.service.portal.MailingListContactService;
import bio.terra.pearl.core.service.portal.PortalEnvironmentService;
import bio.terra.pearl.core.service.portal.PortalService;
import bio.terra.pearl.core.service.study.PortalStudyService;
import bio.terra.pearl.populate.dto.AdminUserDto;
import bio.terra.pearl.populate.dto.PortalEnvironmentPopDto;
import bio.terra.pearl.populate.dto.PortalPopDto;
import bio.terra.pearl.populate.service.contexts.FilePopulateContext;
import bio.terra.pearl.populate.service.contexts.PortalPopulateContext;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class PortalPopulator extends Populator<Portal, FilePopulateContext> {

    private PortalService portalService;

    private PortalEnvironmentService portalEnvironmentService;
    private StudyPopulator studyPopulator;
    private SurveyPopulator surveyPopulator;
    private SiteContentPopulator siteContentPopulator;
    private PortalStudyService portalStudyService;
    private PortalParticipantUserPopulator portalParticipantUserPopulator;
    private MailingListContactService mailingListContactService;
    private AdminUserPopulator adminUserPopulator;



    public PortalPopulator(PortalService portalService,
                           StudyPopulator studyPopulator,
                           PortalStudyService portalStudyService,
                           SiteContentPopulator siteContentPopulator,
                           PortalParticipantUserPopulator portalParticipantUserPopulator,
                           PortalParticipantUserService ppUserService,
                           PortalEnvironmentService portalEnvironmentService, SurveyPopulator surveyPopulator,
                           AdminUserPopulator adminUserPopulator,
                           MailingListContactService mailingListContactService) {
        this.siteContentPopulator = siteContentPopulator;
        this.portalParticipantUserPopulator = portalParticipantUserPopulator;
        this.portalEnvironmentService = portalEnvironmentService;
        this.surveyPopulator = surveyPopulator;
        this.portalService = portalService;
        this.studyPopulator = studyPopulator;
        this.portalStudyService = portalStudyService;
        this.mailingListContactService = mailingListContactService;
        this.adminUserPopulator = adminUserPopulator;
    }

    public Portal populateFromString(String portalContent, FilePopulateContext context) throws IOException {
        PortalPopDto portalDto = objectMapper.readValue(portalContent, PortalPopDto.class);
        Optional<Portal> existingPortal = portalService.findOneByShortcode(portalDto.getShortcode());
        existingPortal.ifPresent(portal ->
            portalService.delete(portal.getId(), new HashSet<>(Arrays.asList(PortalService.AllowedCascades.STUDY)))
        );

        Portal portal = portalService.create(portalDto);
        PortalPopulateContext portalPopContext = new PortalPopulateContext(context, portal.getShortcode(), null);

        siteContentPopulator.populateImages(portalDto.getSiteImageDtos(), portalPopContext);

        for (String surveyFile : portalDto.getSurveyFiles()) {
            surveyPopulator.populate(portalPopContext.newFrom(surveyFile));
        }

        for (PortalEnvironmentPopDto portalEnvironment : portalDto.getPortalEnvironmentDtos()) {
            initializePortalEnv(portalEnvironment, portal, portalPopContext);
        }
        for (String studyFileName : portalDto.getPopulateStudyFiles()) {
            populateStudy(studyFileName, portalPopContext, portal);
        }

        for (AdminUserDto adminUserDto : portalDto.getAdminUsers()) {
            adminUserPopulator.populateForPortal(adminUserDto, portal);
        }
        return portal;
    }

    private void populateStudy(String studyFileName, PortalPopulateContext context, Portal portal) throws IOException {
        Study newStudy = studyPopulator.populate(context.newFrom(studyFileName));
        PortalStudy portalStudy = portalStudyService.create(portal.getId(), newStudy.getId());
        portal.getPortalStudies().add(portalStudy);
        portalStudy.setStudy(newStudy);
    }

    private void initializePortalEnv(PortalEnvironmentPopDto portalEnvPopDto, Portal savedPortal, PortalPopulateContext portalPopContext) throws IOException {
        PortalPopulateContext envConfig = portalPopContext.newFrom(portalEnvPopDto.getEnvironmentName());
        // we're iterating over each population file spec, so now match the current on to the
        // actual entity that got saved as a result of the portal create call.
        PortalEnvironment savedEnv = savedPortal.getPortalEnvironments().stream()
                .filter(env -> env.getEnvironmentName().equals(portalEnvPopDto.getEnvironmentName()))
                .findFirst().get();
        if (portalEnvPopDto.getSiteContentFile() != null) {
            SiteContent content = siteContentPopulator.populate(envConfig.newFrom(portalEnvPopDto.getSiteContentFile()));
            savedEnv.setSiteContent(content);
            savedEnv.setSiteContentId(content.getId());
        }
        if (portalEnvPopDto.getPreRegSurveyDto() != null) {
            Survey matchedSurvey = surveyPopulator.fetchFromPopDto(portalEnvPopDto.getPreRegSurveyDto()).get();
            savedEnv.setPreRegSurveyId(matchedSurvey.getId());
        }
        for (String userFileName : portalEnvPopDto.getParticipantUserFiles()) {
            portalParticipantUserPopulator.populate(envConfig.newFrom(userFileName));
        }
        // re-save the portal environment to update it with any attached siteContents or preRegSurveys
        portalEnvironmentService.update(savedEnv);

        populateMailingList(portalEnvPopDto, savedEnv);
    }

    private void populateMailingList(PortalEnvironmentPopDto portalEnvPopDto, PortalEnvironment savedEnv) {
        for (MailingListContact contact : portalEnvPopDto.getMailingListContacts()) {
            contact.setPortalEnvironmentId(savedEnv.getId());
            contact.setEmail(contact.getEmail());
            contact.setName(contact.getName());
            mailingListContactService.create(contact);
        }
    }
}
