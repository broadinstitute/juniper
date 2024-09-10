package bio.terra.pearl.populate.service;

import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.site.HtmlPage;
import bio.terra.pearl.core.model.site.SiteContent;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.portal.PortalService;
import bio.terra.pearl.core.service.site.SiteContentService;
import bio.terra.pearl.populate.dto.site.*;
import bio.terra.pearl.populate.service.contexts.FilePopulateContext;
import bio.terra.pearl.populate.service.contexts.PortalPopulateContext;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class SiteContentPopulator extends BasePopulator<SiteContent, SiteContentPopDto, PortalPopulateContext> {
    private SiteContentService siteContentService;
    private PortalService portalService;

    public SiteContentPopulator(SiteContentService siteContentService,
                                PortalService portalService) {
        this.portalService = portalService;
        this.siteContentService = siteContentService;
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

    private void initializeNavItem(NavbarItemPopDto navItem, FilePopulateContext config) throws IOException {
        if (navItem.getPopulateFileName() != null) {
            // this is populated from its own file, so read it from there.
            String navPopulateString = filePopulateService.readFile(navItem.getPopulateFileName(), config);
            NavbarItemPopDto popItem = objectMapper.readValue(navPopulateString, NavbarItemPopDto.class);
            BeanUtils.copyProperties(popItem, navItem);
        }

        if (Objects.nonNull(navItem.getHtmlPageDto())) {
            navItem.setHtmlPage(parseHtmlPageDto(navItem.getHtmlPageDto()));
            navItem.setInternalPath(navItem.getHtmlPageDto().getPath());
        }

        if (Objects.nonNull(navItem.getItemDtos())) {
            for (NavbarItemPopDto itemDto : navItem.getItemDtos()) {
                initializeNavItem(itemDto, config);
            }

            navItem.setItems(new ArrayList<>(navItem.getItemDtos()));
        }
    }

    private void initializeLandingPage(LocalizedSiteContentPopDto lscPopDto, FilePopulateContext context) throws IOException {
        if (lscPopDto.getLandingPageFileName() != null) {
            String landingPagePopString = filePopulateService.readFile(lscPopDto.getLandingPageFileName(), context);
            HtmlPagePopDto landingPagePopDto = objectMapper.readValue(landingPagePopString, HtmlPagePopDto.class);
            for (HtmlSectionPopDto sectionPopDto : landingPagePopDto.getSectionDtos()) {
                initializeHtmlSectionDto(sectionPopDto);
            }
            landingPagePopDto.getSections().clear();
            landingPagePopDto.getSections().addAll(landingPagePopDto.getSectionDtos());
            lscPopDto.setLandingPage(landingPagePopDto);
        }
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

    protected void preProcessDto(SiteContentPopDto popDto, PortalPopulateContext context) throws IOException {
        Portal attachedPortal = portalService.findOneByShortcode(context.getPortalShortcode()).get();
        popDto.setPortalId(attachedPortal.getId());
        popDto.setStableId(context.applyShortcodeOverride(popDto.getStableId()));
        for (LocalizedSiteContentPopDto lsc : popDto.getLocalizedSiteContentDtos()) {
            initializeLandingPage(lsc, context);

            for (HtmlPagePopDto page : lsc.getPageDtos()) {
                lsc.getPages().add(parseHtmlPageDto(page));
            }

            for (NavbarItemPopDto navItem : lsc.getNavbarItemDtos()) {
                initializeNavItem(navItem, context);

                // if a navbar item has any html pages in its dto, add it to the top level pages list
                lsc.getPages().addAll(getPages(navItem));
            }
            lsc.getNavbarItems().clear();
            lsc.getNavbarItems().addAll(lsc.getNavbarItemDtos());
            initializeFooterConfig(lsc, context);
        }

        popDto.getLocalizedSiteContents().clear();
        popDto.getLocalizedSiteContents().addAll(popDto.getLocalizedSiteContentDtos());
    }

    private List<HtmlPage> getPages(NavbarItemPopDto navbarItemPopDto) {
        List<HtmlPage> pages = new ArrayList<>();
        if (Objects.nonNull(navbarItemPopDto.getHtmlPageDto())) {
            pages.add(parseHtmlPageDto(navbarItemPopDto.getHtmlPageDto()));
        }

        if (Objects.nonNull(navbarItemPopDto.getItemDtos())) {
            for (NavbarItemPopDto itemDto : navbarItemPopDto.getItemDtos()) {
                pages.addAll(getPages(itemDto));
            }
        }

        return pages;
    }

    @Override
    public Optional<SiteContent> findFromDto(SiteContentPopDto popDto, PortalPopulateContext context) {
        if (popDto.getPopulateFileName() != null) {
            return context.fetchFromPopDto(popDto, siteContentService);
        }
        return siteContentService.findByStableId(popDto.getStableId(), popDto.getVersion(), popDto.getPortalId());
    }

    @Override
    public SiteContent overwriteExisting(SiteContent existingObj, SiteContentPopDto popDto, PortalPopulateContext context) throws IOException {
        siteContentService.delete(existingObj.getId(), CascadeProperty.EMPTY_SET);
        return createNew(popDto, context, true);
    }

    @Override
    public SiteContent createPreserveExisting(SiteContent existingObj, SiteContentPopDto popDto, PortalPopulateContext context) throws IOException {
        int newVersion = siteContentService.getNextVersion(popDto.getStableId(), popDto.getPortalId());
        popDto.setVersion(newVersion);
        return createNew(popDto, context, false);
    }

    @Override
    public SiteContent createNew(SiteContentPopDto popDto, PortalPopulateContext context, boolean overwrite) throws IOException {
        return siteContentService.create(popDto);
    }
}
