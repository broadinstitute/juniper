package bio.terra.pearl.populate.service;

import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.site.HtmlPage;
import bio.terra.pearl.core.model.site.SiteContent;
import bio.terra.pearl.core.model.site.SiteImage;
import bio.terra.pearl.core.service.portal.PortalService;
import bio.terra.pearl.core.service.site.SiteContentService;
import bio.terra.pearl.core.service.site.SiteImageService;
import bio.terra.pearl.populate.dto.site.*;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class SiteContentPopulator extends Populator<SiteContent> {
    private SiteContentService siteContentService;
    private SiteImageService siteImageService;
    private PortalService portalService;

    public SiteContentPopulator(SiteContentService siteContentService,
                                SiteImageService siteImageService,
                                PortalService portalService) {
        this.portalService = portalService;
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
                initializeNavItem(navItem, config);
            }
            lsc.getNavbarItems().clear();
            lsc.getNavbarItems().addAll(lsc.getNavbarItemDtos());
        }
        siteContentDto.getLocalizedSiteContents().clear();
        siteContentDto.getLocalizedSiteContents().addAll(siteContentDto.getLocalizedSiteContentDtos());
        SiteContent savedContent = siteContentService.create(siteContentDto);

        return savedContent;
    }

    /** we need to convert dto properties from json to Strings for storing in the DB. */
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

    public void populateImages(List<SiteImagePopDto> siteImages, FilePopulateConfig config)
            throws IOException {
        for (SiteImagePopDto imageDto : siteImages) {
            String popFileName = imageDto.getPopulateFileName();
            byte[] imageContent = filePopulateService.readBinaryFile(popFileName, config);
            String uploadFileName = imageDto.getUploadFileName();
            if (uploadFileName == null) {
                uploadFileName = popFileName.substring(popFileName.lastIndexOf("/") + 1);
            }
            SiteImage image = SiteImage.builder()
                    .data(imageContent)
                    .portalShortcode(config.getPortalShortcode())
                    .version(imageDto.getVersion() == 0 ? 1 : imageDto.getVersion())
                    .uploadFileName(uploadFileName)
                    .build();
            siteImageService.create(image);
        }
    }

    private void initializeNavItem(NavbarItemPopDto navItem, FilePopulateConfig config) throws IOException {
        if (navItem.getPopulateFileName() != null) {
            // this is populated from its own file, so read it from there.
            String navPopulateString = filePopulateService.readFile(navItem.getPopulateFileName(), config);
            NavbarItemPopDto popItem = objectMapper.readValue(navPopulateString, NavbarItemPopDto.class);
            BeanUtils.copyProperties(popItem, navItem);
        }
        navItem.setHtmlPage(parseHtmlPageDto(navItem.getHtmlPageDto()));
    }

}
