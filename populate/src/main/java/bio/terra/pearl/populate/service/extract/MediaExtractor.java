package bio.terra.pearl.populate.service.extract;

import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.site.SiteMedia;
import bio.terra.pearl.core.service.site.SiteMediaService;
import bio.terra.pearl.populate.dto.site.SiteMediaPopDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MediaExtractor {
    private final SiteMediaService siteMediaService;
    private final ObjectMapper objectMapper;

    public MediaExtractor(SiteMediaService siteMediaService, @Qualifier("extractionObjectMapper") ObjectMapper objectMapper) {
        this.siteMediaService = siteMediaService;
        this.objectMapper = objectMapper;
    }

    public void writeMedia(Portal portal, ExtractPopulateContext context) {
        List<SiteMedia> images = siteMediaService.findByPortal(portal.getShortcode());
        for (SiteMedia image : images) {
            writeMedia(image, context);
        }
    }

    public void writeMedia(SiteMedia media, ExtractPopulateContext context) {
        try {
            String filename = "media/%s".formatted(media.getCleanFileName());
            if (media.getVersion() != 1) {
                filename = "media/%d-%s".formatted(media.getVersion(), media.getCleanFileName());
            }
            context.writeFileForEntity(filename, media.getData(), media.getId());
            SiteMediaPopDto mediaPopDto = new SiteMediaPopDto();
            mediaPopDto.setPopulateFileName(filename);
            mediaPopDto.setVersion(media.getVersion());
            if (media.getVersion() != 1) {
                mediaPopDto.setCleanFileName(media.getCleanFileName());
            }
            context.getPortalPopDto().getSiteMediaDtos().add(mediaPopDto);
        } catch (Exception e) {
            throw new RuntimeException("Error writing media %s to json".formatted(media.getCleanFileName()), e);
        }
    }

}
