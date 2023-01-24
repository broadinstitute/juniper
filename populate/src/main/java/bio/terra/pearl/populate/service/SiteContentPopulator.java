package bio.terra.pearl.populate.service;

import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.site.HtmlPage;
import bio.terra.pearl.core.model.site.SiteContent;
import bio.terra.pearl.core.model.site.SiteImage;
import bio.terra.pearl.core.service.portal.PortalService;
import bio.terra.pearl.core.service.site.SiteContentService;
import bio.terra.pearl.core.service.site.SiteImageService;
import bio.terra.pearl.populate.dto.site.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class SiteContentPopulator extends Populator<SiteContent> {
    private SiteContentService siteContentService;
    private SiteImageService siteImageService;
    private PortalService portalService;

    public SiteContentPopulator(SiteContentService siteContentService,
                                SiteImageService siteImageService,
                                FilePopulateService filePopulateService,
                                ObjectMapper objectMapper, PortalService portalService) {
        this.portalService = portalService;
        this.objectMapper = objectMapper;
        this.filePopulateService = filePopulateService;
        this.siteContentService = siteContentService;
        this.siteImageService = siteImageService;
    }

    @Override
    public SiteContent populateFromString(String fileString, FilePopulateConfig config) throws IOException {
        SiteContentPopDto siteContentDto = objectMapper.readValue(fileString, SiteContentPopDto.class);
        Optional<SiteContent> existingContent = siteContentService
                .findOne(siteContentDto.getStableId(), siteContentDto.getVersion());
        if (existingContent.isPresent()) {
            // for now, assume that if it exists, it has already been refreshed via populating another environment.
            return existingContent.get();
        };
        Portal attachedPortal = portalService.findOneByShortcode(config.getPortalShortcode()).get();
        siteContentDto.setPortalId(attachedPortal.getId());
        for (LocalizedSiteContentPopDto lsc : siteContentDto.getLocalizedSiteContentDtos()) {
            lsc.setLandingPage(parseHtmlPageDto(lsc.getLandingPage()));
            for (NavbarItemPopDto navItem : lsc.getNavbarItemDtos()) {
                navItem.setHtmlPage(parseHtmlPageDto(navItem.getHtmlPageDto()));
            }
            lsc.getNavbarItems().clear();
            lsc.getNavbarItems().addAll(lsc.getNavbarItemDtos());
        }
        siteContentDto.getLocalizedSiteContents().clear();
        siteContentDto.getLocalizedSiteContents().addAll(siteContentDto.getLocalizedSiteContentDtos());
        SiteContent savedContent = siteContentService.create(siteContentDto);
        populateImages(siteContentDto, savedContent.getId(), config);

        return savedContent;
    }

    private HtmlPage parseHtmlPageDto(HtmlPagePopDto page) {
        if (page != null) {
            for (HtmlSectionPopDto section : page.getSectionDtos()) {
                if (section.getSectionConfigJson() != null)  {
                    section.setSectionConfig(section.getSectionConfigJson().toString());
                }
            }
        }
        page.getSections().clear();
        page.getSections().addAll(page.getSectionDtos());
        return page;
    }

    private void populateImages(SiteContentPopDto siteContent, UUID portalEnvId, FilePopulateConfig config)
            throws IOException {
        for (SiteImagePopDto imageDto : siteContent.getSiteImageDtos()) {
            String popFileName = imageDto.getPopulateFileName();
            byte[] imageContent = filePopulateService.readBinaryFile(popFileName, config);
            String uploadFileName = imageDto.getUploadFileName();
            if (uploadFileName == null) {
                uploadFileName = popFileName.substring(popFileName.lastIndexOf("/") + 1);
            }
            SiteImage image = SiteImage.builder()
                    .data(imageContent)
                    .siteContentId(portalEnvId)
                    .uploadFileName(uploadFileName)
                    .build();
            siteImageService.create(image);
        }
    }

}
