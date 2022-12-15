package bio.terra.pearl.populate.service;

import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.site.SiteContent;
import bio.terra.pearl.core.model.site.SiteImage;
import bio.terra.pearl.core.model.study.PortalStudy;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.service.participant.PortalParticipantUserService;
import bio.terra.pearl.core.service.portal.PortalService;
import bio.terra.pearl.core.service.site.SiteContentService;
import bio.terra.pearl.core.service.site.SiteImageService;
import bio.terra.pearl.core.service.study.PortalStudyService;
import bio.terra.pearl.populate.dto.PortalEnvironmentPopDto;
import bio.terra.pearl.populate.dto.PortalPopDto;
import bio.terra.pearl.populate.dto.site.SiteContentPopDto;
import bio.terra.pearl.populate.dto.site.SiteImagePopDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

@Service
public class PortalPopulator extends Populator<Portal> {

    private PortalService portalService;
    private StudyPopulator studyPopulator;
    private PortalStudyService portalStudyService;
    private PortalParticipantUserPopulator portalParticipantUserPopulator;
    private PortalParticipantUserService ppUserService;
    private SiteContentService siteContentService;
    private SiteImageService siteImageService;


    public PortalPopulator(FilePopulateService filePopulateService,
                           PortalService portalService,
                           StudyPopulator studyPopulator,
                           PortalStudyService portalStudyService,
                           ObjectMapper objectMapper,
                           PortalParticipantUserPopulator portalParticipantUserPopulator,
                           PortalParticipantUserService ppUserService,
                           SiteContentService siteContentService,
                           SiteImageService siteImageService) {
        this.portalParticipantUserPopulator = portalParticipantUserPopulator;
        this.ppUserService = ppUserService;
        this.siteContentService = siteContentService;
        this.siteImageService = siteImageService;
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
        for (PortalEnvironmentPopDto portalEnvironment : portalDto.getPortalEnvironmentDtos()) {
            if (portalEnvironment.getSiteContent() != null) {
                SiteContentPopDto siteContentDto = portalEnvironment.getSiteContent();
                SiteContent savedContent = siteContentService.create(siteContentDto);
                populateImages(siteContentDto, savedContent.getId(), config);
            }
            for (String userFileName : portalEnvironment.getParticipantUserFiles()) {
                populateParticipantUser(userFileName, config,
                        portal.getShortcode(), portalEnvironment.getEnvironmentName());
            }
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

    private void populateImages(SiteContentPopDto siteContent, UUID portalEnvId,  FilePopulateConfig config)
            throws IOException {
        for (SiteImagePopDto imageDto : siteContent.getSiteImageDtos()) {
            byte[] imageContent = filePopulateService.readBinaryFile(imageDto.getPopulateFileName(), config);
            SiteImage image = SiteImage.builder()
                    .data(imageContent)
                    .siteContentId(portalEnvId)
                    .uploadFileName(imageDto.getUploadFileName())
                    .build();
            siteImageService.create(image);
        }
    }

}
