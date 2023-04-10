package bio.terra.pearl.populate.service;

import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.site.HtmlPage;
import bio.terra.pearl.core.model.site.SiteContent;
import bio.terra.pearl.core.model.site.SiteImage;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.portal.PortalService;
import bio.terra.pearl.core.service.site.SiteContentService;
import bio.terra.pearl.core.service.site.SiteImageService;
import bio.terra.pearl.populate.dto.site.*;
import bio.terra.pearl.populate.service.contexts.FilePopulateContext;
import bio.terra.pearl.populate.service.contexts.PortalPopulateContext;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class SiteContentPopulator extends Populator<SiteContent, SiteContentPopDto, PortalPopulateContext> {
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

    /** we need to convert dto properties from json to Strings for storing in the DB. */
    private HtmlPage parseHtmlPageDto(HtmlPagePopDto page) {
        if (page != null) {
            for (HtmlSectionPopDto section : page.getSectionDtos()) {
                initializeHtmlSectionDto(section);
            }
            page.getSections().clear();
            page.getSections().addAll(page.getSectionDtos());
        }
        return page;
    }

    private void initializeHtmlSectionDto(HtmlSectionPopDto sectionPopDto) {
        if (sectionPopDto.getSectionConfigJson() != null)  {
            sectionPopDto.setSectionConfig(sectionPopDto.getSectionConfigJson().toString());
        }
    }

    public void populateImages(List<SiteImagePopDto> siteImages, PortalPopulateContext context, boolean overwrite)
            throws IOException {
        for (SiteImagePopDto imageDto : siteImages) {
            SiteImage image = convertDto(imageDto, context);
            // we don't have to worry about deleting if it's overwrite -- the portal delete will have already deleted the images
            if (!overwrite) {
                int nextVersion = siteImageService.getNextVersion(image.getCleanFileName(), image.getPortalShortcode());
                image.setVersion(nextVersion);
            }
            SiteImage savedImage = siteImageService.create(image);
            context.markFilenameAsPopulated(imageDto.getPopulateFileName(), savedImage.getId());
        }
    }

    private SiteImage convertDto(SiteImagePopDto imageDto, PortalPopulateContext context) throws IOException {
        String popFileName = imageDto.getPopulateFileName();
        byte[] imageContent = filePopulateService.readBinaryFile(popFileName, context);
        String uploadFileName = imageDto.getUploadFileName();
        if (uploadFileName == null) {
            uploadFileName = popFileName.substring(popFileName.lastIndexOf("/") + 1);
        }
        return SiteImage.builder()
                .data(imageContent)
                .portalShortcode(context.getPortalShortcode())
                .version(imageDto.getVersion() == 0 ? 1 : imageDto.getVersion())
                .uploadFileName(uploadFileName)
                .build();
    }

    private void initializeNavItem(NavbarItemPopDto navItem, FilePopulateContext config) throws IOException {
        if (navItem.getPopulateFileName() != null) {
            // this is populated from its own file, so read it from there.
            String navPopulateString = filePopulateService.readFile(navItem.getPopulateFileName(), config);
            NavbarItemPopDto popItem = objectMapper.readValue(navPopulateString, NavbarItemPopDto.class);
            BeanUtils.copyProperties(popItem, navItem);
        }
        navItem.setHtmlPage(parseHtmlPageDto(navItem.getHtmlPageDto()));
    }

    private void initializeFooterConfig(LocalizedSiteContentPopDto lscPopDto, FilePopulateContext context) throws IOException {
        if (lscPopDto.getFooterSectionFile() != null) {
            String footerPopString = filePopulateService.readFile(lscPopDto.getFooterSectionFile(), context);
            HtmlSectionPopDto sectionPopDto = objectMapper.readValue(footerPopString, HtmlSectionPopDto.class);
            initializeHtmlSectionDto(sectionPopDto);
            lscPopDto.setFooterSection(sectionPopDto);
        }
    }

    @Override
    protected Class<SiteContentPopDto> getDtoClazz() {
        return SiteContentPopDto.class;
    }

    protected void updateDtoFromContext(SiteContentPopDto popDto, PortalPopulateContext context) throws IOException {
        Portal attachedPortal = portalService.findOneByShortcode(context.getPortalShortcode()).get();
        popDto.setPortalId(attachedPortal.getId());
        for (LocalizedSiteContentPopDto lsc : popDto.getLocalizedSiteContentDtos()) {
            lsc.setLandingPage(parseHtmlPageDto(lsc.getLandingPage()));
            for (NavbarItemPopDto navItem : lsc.getNavbarItemDtos()) {
                initializeNavItem(navItem, context);
            }
            lsc.getNavbarItems().clear();
            lsc.getNavbarItems().addAll(lsc.getNavbarItemDtos());
            initializeFooterConfig(lsc, context);
        }

        popDto.getLocalizedSiteContents().clear();
        popDto.getLocalizedSiteContents().addAll(popDto.getLocalizedSiteContentDtos());
    }

    @Override
    public Optional<SiteContent> findFromDto(SiteContentPopDto popDto, PortalPopulateContext context) {
        if (popDto.getPopulateFileName() != null) {
            return context.fetchFromPopDto(popDto, siteContentService);
        }
        return siteContentService.findByStableId(popDto.getStableId(), popDto.getVersion());
    }

    @Override
    public SiteContent overwriteExisting(SiteContent existingObj, SiteContentPopDto popDto, PortalPopulateContext context) throws IOException {
        siteContentService.delete(existingObj.getId(), CascadeProperty.EMPTY_SET);
        return createNew(popDto, context, true);
    }

    @Override
    public SiteContent createPreserveExisting(SiteContent existingObj, SiteContentPopDto popDto, PortalPopulateContext context) throws IOException {
        int newVersion = siteContentService.getNextVersion(popDto.getStableId());
        popDto.setVersion(newVersion);
        return createNew(popDto, context, false);
    }

    @Override
    public SiteContent createNew(SiteContentPopDto popDto, PortalPopulateContext context, boolean overwrite) throws IOException {
        return siteContentService.create(popDto);
    }
}
