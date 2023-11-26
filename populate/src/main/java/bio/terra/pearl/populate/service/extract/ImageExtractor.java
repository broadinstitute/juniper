package bio.terra.pearl.populate.service.extract;

import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.site.SiteImage;
import bio.terra.pearl.core.service.site.SiteImageService;
import bio.terra.pearl.populate.dto.site.SiteImagePopDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ImageExtractor {
    private final SiteImageService siteImageService;
    private final ObjectMapper objectMapper;

    public ImageExtractor(SiteImageService siteImageService, ObjectMapper objectMapper) {
        this.siteImageService = siteImageService;
        this.objectMapper = objectMapper;
    }

    public void writeImages(Portal portal, ExtractPopulateContext context) {
        List<SiteImage> images = siteImageService.findByPortal(portal.getShortcode());
        for (SiteImage image : images) {
            writeImage(image, context);
        }
    }

    public void writeImage(SiteImage image, ExtractPopulateContext context) {
        try {
            String filename = "images/%s".formatted(image.getCleanFileName());
            if (image.getVersion() != 1) {
                filename = "images/%d-%s".formatted(image.getVersion(), image.getCleanFileName());
            }
            context.writeFileForEntity(filename, image.getData(), image.getId());
            SiteImagePopDto imagePopDto = new SiteImagePopDto();
            imagePopDto.setPopulateFileName(filename);
            imagePopDto.setVersion(image.getVersion());
            if (image.getVersion() != 1) {
                imagePopDto.setCleanFileName(image.getCleanFileName());
            }
            context.getPortalPopDto().getSiteImageDtos().add(imagePopDto);
        } catch (Exception e) {
            throw new RuntimeException("Error writing image %s to json".formatted(image.getCleanFileName()), e);
        }
    }

}
