package bio.terra.pearl.populate.service.extract;

import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.site.*;
import bio.terra.pearl.core.service.site.SiteContentService;
import bio.terra.pearl.populate.dto.site.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SiteContentExtractor {

    private final SiteContentService siteContentService;
    private final ObjectMapper objectMapper;

    public SiteContentExtractor(SiteContentService siteContentService, @Qualifier("extractionObjectMapper") ObjectMapper objectMapper) {
        this.siteContentService = siteContentService;
        this.objectMapper = objectMapper;
    }

    public void writeSiteContents(Portal portal, ExportPopulateContext context) {
        List<SiteContent> siteContents = siteContentService.findByPortalId(portal.getId());
        for (SiteContent siteContent : siteContents) {
            siteContentService.attachChildContent(siteContent, "en");
            writeSiteContent(siteContent, context);
        }
    }

    public void writeSiteContent(SiteContent siteContent, ExportPopulateContext context) {
        SiteContentPopDto siteContentPopDto = new SiteContentPopDto();
        BeanUtils.copyProperties(siteContent, siteContentPopDto, "id", "portalId", "localizedSiteContents");
        for (LocalizedSiteContent localSite : siteContent.getLocalizedSiteContents()) {
            try {
                LocalizedSiteContentPopDto localPopDto = convertLocalizedSiteContent(localSite, context);
                siteContentPopDto.getLocalizedSiteContentDtos().add(localPopDto);
            } catch (Exception e) {
                throw new RuntimeException("Error writing siteContent %s-%s to json".formatted(siteContent.getStableId(), localSite.getLanguage()), e);
            }
        }
        try {
            String fileString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(siteContentPopDto);
            context.writeFileForEntity("siteContent.json", fileString, siteContent.getId());
            context.getPortalPopDto().getSiteContentFiles().add("siteContent.json");
        } catch (Exception e) {
            throw new RuntimeException("Error writing portal to json", e);
        }
    }

    public LocalizedSiteContentPopDto convertLocalizedSiteContent(LocalizedSiteContent lsc, ExportPopulateContext context) throws JsonProcessingException {
        LocalizedSiteContentPopDto localPopDto = new LocalizedSiteContentPopDto();
        BeanUtils.copyProperties(lsc, localPopDto, "id", "siteContentId", "navbarItems", "footerSection", "footerSectionId", "landingPage", "landingPageId");
        for (NavbarItem navbarItem : lsc.getNavbarItems()) {
            localPopDto.getNavbarItemDtos().add(convertNavbarItem(navbarItem, lsc, context));
        }
        if (lsc.getFooterSection() != null) {
            String footerFile = "siteContent/footer-%s.json".formatted(lsc.getLanguage());
            localPopDto.setFooterSectionFile(footerFile);
            HtmlSectionPopDto footerSectionPopDto = convertHtmlSection(lsc.getFooterSection());
            String footerAsString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(footerSectionPopDto);
            context.writeFileForEntity(footerFile, footerAsString, lsc.getFooterSection().getId());
        }
        if (lsc.getLandingPage() != null) {
            localPopDto.setLandingPage(convertHtmlPage(lsc.getLandingPage()));
        }
        return localPopDto;
    }

    public NavbarItemPopDto convertNavbarItem(NavbarItem navbarItem, LocalizedSiteContent lsc, ExportPopulateContext context) throws JsonProcessingException {
        NavbarItemPopDto navbarItemPopDto = new NavbarItemPopDto();
        if (navbarItem.getItemType().equals(NavbarItemType.INTERNAL)) {
            String navbarFile = "siteContent/page-%s-%s.json".formatted(navbarItem.getHtmlPage().getPath(), lsc.getLanguage());
            navbarItemPopDto.setPopulateFileName(navbarFile);

            NavbarItemPopDto itemFileDto = new NavbarItemPopDto();
            BeanUtils.copyProperties(navbarItem, itemFileDto, "id", "localizedSiteContentId", "htmlPage");
            itemFileDto.setHtmlPageDto(convertHtmlPage(navbarItem.getHtmlPage()));
            String navbarAsString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(itemFileDto);
            context.writeFileForEntity(navbarFile, navbarAsString, navbarItem.getId());
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
        try {
            htmlSectionPopDto.setSectionConfigJson(objectMapper.readTree(htmlSection.getSectionConfig()));
        } catch (Exception e) {
            throw new RuntimeException("Error converting section config to json", e);
        }
        return htmlSectionPopDto;
    }
}
