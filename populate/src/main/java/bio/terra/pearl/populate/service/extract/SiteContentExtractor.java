package bio.terra.pearl.populate.service.extract;

import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.site.*;
import bio.terra.pearl.core.service.site.SiteContentService;
import bio.terra.pearl.populate.dto.site.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class SiteContentExtractor {

    private final SiteContentService siteContentService;
    private final ObjectMapper objectMapper;

    public SiteContentExtractor(SiteContentService siteContentService, @Qualifier("extractionObjectMapper") ObjectMapper objectMapper) {
        this.siteContentService = siteContentService;
        this.objectMapper = objectMapper;
        this.objectMapper.addMixIn(NavbarItem .class, NavbarItemMixin.class);
        this.objectMapper.addMixIn(HtmlSection.class, HtmlSectionMixin.class);
    }

    public void writeSiteContents(Portal portal, ExtractPopulateContext context) {
        List<SiteContent> siteContents = siteContentService.findByPortalId(portal.getId());
        for (SiteContent siteContent : siteContents) {
            siteContentService.attachChildContent(siteContent, "en");
            writeSiteContent(siteContent, context);
        }
    }

    public void writeSiteContent(SiteContent siteContent, ExtractPopulateContext context) {
        SiteContentPopDto siteContentPopDto = new SiteContentPopDto();
        BeanUtils.copyProperties(siteContent, siteContentPopDto, "id", "portalId", "localizedSiteContents");
        String filePath = filePathForSiteContent(siteContent);
        for (LocalizedSiteContent localSite : siteContent.getLocalizedSiteContents()) {
            try {
                LocalizedSiteContentPopDto localPopDto = convertLocalizedSiteContent(localSite, filePath, context);
                siteContentPopDto.getLocalizedSiteContentDtos().add(localPopDto);
            } catch (Exception e) {
                throw new RuntimeException("Error writing siteContent %s %s to json".formatted(filePath, localSite.getLanguage()), e);
            }
        }
        try {
            String fileString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(siteContentPopDto);
            context.writeFileForEntity(filePath + "/siteContent.json", fileString, siteContent.getId());
            context.getPortalPopDto().getSiteContentFiles().add(filePath + "/siteContent.json");
        } catch (Exception e) {
            throw new RuntimeException("Error writing portal to json", e);
        }
    }

    public LocalizedSiteContentPopDto convertLocalizedSiteContent(LocalizedSiteContent lsc, String filePath, ExtractPopulateContext context) throws JsonProcessingException {
        LocalizedSiteContentPopDto localPopDto = new LocalizedSiteContentPopDto();
        BeanUtils.copyProperties(lsc, localPopDto, "id", "siteContentId", "navbarItems", "footerSection", "footerSectionId", "landingPage", "landingPageId");
        for (NavbarItem navbarItem : lsc.getNavbarItems()) {
            localPopDto.getNavbarItemDtos().add(convertNavbarItem(navbarItem, lsc, filePath, context));
        }
        if (lsc.getFooterSection() != null) {
            String footerFile = "%s/footer.json".formatted(lsc.getLanguage());
            localPopDto.setFooterSectionFile(footerFile);
            HtmlSectionPopDto footerSectionPopDto = convertHtmlSection(lsc.getFooterSection());
            String footerAsString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(footerSectionPopDto);
            context.writeFileForEntity(filePath + "/" + footerFile, footerAsString, lsc.getFooterSection().getId());
        }
        if (lsc.getLandingPage() != null) {
            String landingPageFile = "%s/landingPage.json".formatted(lsc.getLanguage());
            localPopDto.setLandingPageFileName(landingPageFile);
            HtmlPagePopDto landingPagePopDto = convertHtmlPage(lsc.getLandingPage());
            String landingPageAsString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(landingPagePopDto);
            context.writeFileForEntity(filePath + "/" + landingPageFile, landingPageAsString, lsc.getLandingPage().getId());
        }
        return localPopDto;
    }

    public NavbarItemPopDto convertNavbarItem(NavbarItem navbarItem, LocalizedSiteContent lsc, String filePath, ExtractPopulateContext context) throws JsonProcessingException {
        NavbarItemPopDto navbarItemPopDto = new NavbarItemPopDto();
        if (navbarItem.getItemType().equals(NavbarItemType.INTERNAL)) {
            String navbarFile = "%s/page-%s.json".formatted(lsc.getLanguage(), navbarItem.getHtmlPage().getPath());
            navbarItemPopDto.setPopulateFileName(navbarFile);

            NavbarItemPopDto itemFileDto = new NavbarItemPopDto();
            BeanUtils.copyProperties(navbarItem, itemFileDto, "id", "localizedSiteContentId", "htmlPage");
            itemFileDto.setHtmlPageDto(convertHtmlPage(navbarItem.getHtmlPage()));
            String navbarAsString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(itemFileDto);
            context.writeFileForEntity(filePath + "/" + navbarFile, navbarAsString, navbarItem.getId());
        } else {
            BeanUtils.copyProperties(navbarItem, navbarItemPopDto, "id", "localizedSiteContentId");
        }
        return navbarItemPopDto;
    }

    public HtmlPagePopDto convertHtmlPage(HtmlPage htmlPage) {
        HtmlPagePopDto htmlPagePopDto = new HtmlPagePopDto();
        BeanUtils.copyProperties(htmlPage, htmlPagePopDto, "id", "localizedSiteContentId", "sections");
        for (HtmlSection htmlSection : htmlPage.getSections()) {
            htmlPagePopDto.getSectionDtos().add(convertHtmlSection(htmlSection));
        }
        return htmlPagePopDto;
    }

    public HtmlSectionPopDto convertHtmlSection(HtmlSection htmlSection) {
        HtmlSectionPopDto htmlSectionPopDto = new HtmlSectionPopDto();
        BeanUtils.copyProperties(htmlSection, htmlSectionPopDto, "id", "htmlPageId", "sectionConfig");
        if (htmlSection.getSectionConfig() != null) {
            // this is not a raw html section, so write the config as json for legibility
            try {
                htmlSectionPopDto.setSectionConfigJson(objectMapper.readTree(htmlSection.getSectionConfig()));
            } catch (Exception e) {
                throw new RuntimeException("Error converting section config to json", e);
            }
        }
        return htmlSectionPopDto;
    }

    /** don't serialize the item order -- it's captured in the order of the list */
    protected static class NavbarItemMixin {
        @JsonIgnore
        public UUID getItemOrder() {return null;}
    }

    /** don't serialize the section order -- it's captured in the order of the list */
    protected static class HtmlSectionMixin {
        @JsonIgnore
        public UUID getSectionOrder() {return null;}
    }

    /** stub class for just writing out the file name */
    protected static class SiteContentPopDtoStub extends SiteContentPopDto {
        @JsonIgnore @Override
        public List<LocalizedSiteContent> getLocalizedSiteContents() { return null; }
        @JsonIgnore @Override
        public List<LocalizedSiteContentPopDto> getLocalizedSiteContentDtos() { return null; }
        @JsonIgnore @Override
        public int getVersion() { return 0; }
    }

    public static String filePathForSiteContent(SiteContent siteContent) {
        return "siteContent/%s-%s".formatted(siteContent.getStableId(), siteContent.getVersion());
    }
}
