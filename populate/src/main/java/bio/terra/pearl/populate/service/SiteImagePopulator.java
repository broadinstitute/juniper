package bio.terra.pearl.populate.service;

import bio.terra.pearl.core.model.site.SiteImage;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.site.SiteImageService;
import bio.terra.pearl.populate.dto.site.SiteImagePopDto;
import bio.terra.pearl.populate.service.contexts.PortalPopulateContext;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class SiteImagePopulator extends BasePopulator<SiteImage, SiteImagePopDto, PortalPopulateContext> {
    private SiteImageService siteImageService;

    public SiteImagePopulator(SiteImageService siteImageService) {
        this.siteImageService = siteImageService;
    }

    @Override
    protected Class<SiteImagePopDto> getDtoClazz() {
        return SiteImagePopDto.class;
    }

    @Override
    protected void preProcessDto(SiteImagePopDto popDto, PortalPopulateContext context) throws IOException {
        String popFileName = popDto.getPopulateFileName();
        byte[] imageContent = filePopulateService.readBinaryFile(popFileName, context);
        if (popDto.getUploadFileName() == null) {
            popDto.setUploadFileName(popDto.getPopulateFileName().substring(popFileName.lastIndexOf("/") + 1));
        }
        popDto.setData(imageContent);
        popDto.setPortalShortcode(context.getPortalShortcode());
        popDto.setVersion(popDto.getVersion() == 0 ? 1 : popDto.getVersion());
        popDto.setCleanFileName(SiteImageService.cleanFileName(popDto.getUploadFileName()));
    }

    @Override
    public Optional<SiteImage> findFromDto(SiteImagePopDto popDto, PortalPopulateContext context) {
        Optional<SiteImage> imageOpt = context.fetchFromPopDto(popDto, siteImageService);
        if (imageOpt.isPresent()) {
            return imageOpt;
        }
        return siteImageService.findOne(context.getPortalShortcode(), popDto.getCleanFileName(), popDto.getVersion());
    }

    @Override
    public SiteImage overwriteExisting(SiteImage existingObj, SiteImagePopDto popDto, PortalPopulateContext context) throws IOException {
        siteImageService.delete(existingObj.getId(), CascadeProperty.EMPTY_SET);
        return createNew(popDto, context, true);
    }

    @Override
    public SiteImage createPreserveExisting(SiteImage existingObj, SiteImagePopDto popDto, PortalPopulateContext context) throws IOException {
        if (Arrays.equals(existingObj.getData(), popDto.getData())) {
            // the things are the same, don't bother creating a new version
            return existingObj;
        }
        int newVersion = siteImageService.getNextVersion(popDto.getCleanFileName(), popDto.getPortalShortcode());
        popDto.setVersion(newVersion);
        return createNew(popDto, context, false);
    }

    @Override
    public SiteImage createNew(SiteImagePopDto popDto, PortalPopulateContext context, boolean overwrite) throws IOException {
        return siteImageService.create(popDto);
    }
}
