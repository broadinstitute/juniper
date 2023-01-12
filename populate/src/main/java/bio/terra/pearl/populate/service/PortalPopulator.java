package bio.terra.pearl.populate.service;

import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.site.SiteContent;
import bio.terra.pearl.core.model.study.PortalStudy;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.service.participant.PortalParticipantUserService;
import bio.terra.pearl.core.service.portal.PortalEnvironmentService;
import bio.terra.pearl.core.service.portal.PortalService;
import bio.terra.pearl.core.service.study.PortalStudyService;
import bio.terra.pearl.populate.dto.PortalEnvironmentPopDto;
import bio.terra.pearl.populate.dto.PortalPopDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PortalPopulator extends Populator<Portal> {

    private PortalService portalService;

    private PortalEnvironmentService portalEnvironmentService;
    private StudyPopulator studyPopulator;
    private SurveyPopulator surveyPopulator;
    private SiteContentPopulator siteContentPopulator;
    private PortalStudyService portalStudyService;
    private PortalParticipantUserPopulator portalParticipantUserPopulator;


    public PortalPopulator(FilePopulateService filePopulateService,
                           ObjectMapper objectMapper,
                           PortalService portalService,
                           StudyPopulator studyPopulator,
                           PortalStudyService portalStudyService,
                           SiteContentPopulator siteContentPopulator,
                           PortalParticipantUserPopulator portalParticipantUserPopulator,
                           PortalParticipantUserService ppUserService,
                           PortalEnvironmentService portalEnvironmentService, SurveyPopulator surveyPopulator) {
        this.siteContentPopulator = siteContentPopulator;
        this.portalParticipantUserPopulator = portalParticipantUserPopulator;
        this.portalEnvironmentService = portalEnvironmentService;
        this.surveyPopulator = surveyPopulator;
        this.filePopulateService = filePopulateService;
        this.portalService = portalService;
        this.studyPopulator = studyPopulator;
        this.portalStudyService = portalStudyService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    @Override
    public Portal populate(String filePathName) throws IOException {
        FilePopulateConfig config = new FilePopulateConfig(filePathName);
        String portalFileString = filePopulateService.readFile(config.getRootFileName(), config);
        return populateFromString(portalFileString, config);
    }

    public Portal populateFromString(String portalContent, FilePopulateConfig config) throws IOException {
        PortalPopDto portalDto = objectMapper.readValue(portalContent, PortalPopDto.class);
        Optional<Portal> existingPortal = portalService.findOneByShortcode(portalDto.getShortcode());
        existingPortal.ifPresent(portal ->
            portalService.delete(portal.getId(), new HashSet<>(Arrays.asList(PortalService.AllowedCascades.STUDY)))
        );

        Portal portal = portalService.create(portalDto);
        // first, populate the surveys
        for (String surveyFile : portalDto.getSurveyFiles()) {
            surveyPopulator.populate(config.newForPortal(surveyFile, portal.getShortcode(), null));
        }

        for (PortalEnvironmentPopDto portalEnvironment : portalDto.getPortalEnvironmentDtos()) {
            // we're iterating over each population file spec, so now match the current on to the
            // actual entity that got saved as a result of the portal create call.
            PortalEnvironment savedEnv = portal.getPortalEnvironments().stream()
                    .filter(env -> env.getEnvironmentName().equals(portalEnvironment.getEnvironmentName()))
                    .findFirst().get();
            if (portalEnvironment.getSiteContentFile() != null) {
                SiteContent content = siteContentPopulator.populate(config.newForPortal(
                        portalEnvironment.getSiteContentFile(),
                        portal.getShortcode(),
                        portalEnvironment.getEnvironmentName()));
                savedEnv.setSiteContent(content);
                savedEnv.setSiteContentId(content.getId());
            }
            if (portalEnvironment.getPreRegSurveyDto() != null) {
                Survey matchedSurvey = surveyPopulator.fetchFromPopDto(portalEnvironment.getPreRegSurveyDto()).get();
                savedEnv.setPreRegSurveyId(matchedSurvey.getId());
            }
            for (String userFileName : portalEnvironment.getParticipantUserFiles()) {
                populateParticipantUser(userFileName, config,
                        portal.getShortcode(), portalEnvironment.getEnvironmentName());
            }
            // re-save the portal environment to update it with any attached siteContents or preRegSurveys
            portalEnvironmentService.update(savedEnv);
        }
        for (String studyFileName : portalDto.getPopulateStudyFiles()) {
            populateStudy(studyFileName, config, portal);
        }
        return portal;
    }

    private void populateStudy(String studyFileName, FilePopulateConfig config, Portal portal) throws IOException {
        Study newStudy = studyPopulator.populate(config.newForPortal(studyFileName, portal.getShortcode(), null));
        PortalStudy portalStudy = portalStudyService.create(portal.getId(), newStudy.getId());
        portal.getPortalStudies().add(portalStudy);
        portalStudy.setStudy(newStudy);
    }

    private void populateParticipantUser(String userFileName, FilePopulateConfig config,
                                         String portalShortcode, EnvironmentName envName) throws IOException {
        PortalParticipantUser ppUser = portalParticipantUserPopulator.populate(
                config.newForPortal(userFileName, portalShortcode, envName)
        );
    }
}
