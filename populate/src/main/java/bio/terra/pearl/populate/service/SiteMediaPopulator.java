package bio.terra.pearl.populate.service;

import bio.terra.pearl.core.model.site.SiteMedia;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.site.SiteMediaService;
import bio.terra.pearl.populate.dto.site.SiteMediaPopDto;
import bio.terra.pearl.populate.service.contexts.PortalPopulateContext;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

@Service
public class SiteMediaPopulator extends BasePopulator<SiteMedia, SiteMediaPopDto, PortalPopulateContext> {
    private SiteMediaService siteMediaService;

    public SiteMediaPopulator(SiteMediaService siteMediaService) {
        this.siteMediaService = siteMediaService;
    }

    @Override
    protected Class<SiteMediaPopDto> getDtoClazz() {
        return SiteMediaPopDto.class;
    }

    @Override
    protected void preProcessDto(SiteMediaPopDto popDto, PortalPopulateContext context) throws IOException {
        String popFileName = popDto.getPopulateFileName();
        byte[] imageContent = filePopulateService.readBinaryFile(popFileName, context);
        if (popDto.getCleanFileName() == null) {
            popDto.setCleanFileName(popDto.getPopulateFileName().substring(popFileName.lastIndexOf("/") + 1));
        }
        popDto.setData(imageContent);
        popDto.setPortalShortcode(context.getPortalShortcode());
        popDto.setVersion(popDto.getVersion() == 0 ? 1 : popDto.getVersion());
    }

    @Override
    public Optional<SiteMedia> findFromDto(SiteMediaPopDto popDto, PortalPopulateContext context) {
        Optional<SiteMedia> imageOpt = context.fetchFromPopDto(popDto, siteMediaService);
        if (imageOpt.isPresent()) {
            return imageOpt;
        }
        return siteMediaService.findOne(context.getPortalShortcode(), popDto.getCleanFileName(), popDto.getVersion());
    }

    @Override
    public SiteMedia overwriteExisting(SiteMedia existingObj, SiteMediaPopDto popDto, PortalPopulateContext context) throws IOException {
        siteMediaService.delete(existingObj.getId(), CascadeProperty.EMPTY_SET);
        return createNew(popDto, context, true);
    }

    @Override
    public SiteMedia createPreserveExisting(SiteMedia existingObj, SiteMediaPopDto popDto, PortalPopulateContext context) throws IOException {
        if (Arrays.equals(existingObj.getData(), popDto.getData())) {
            // the things are the same, don't bother creating a new version
            return existingObj;
        }
        int newVersion = siteMediaService.getNextVersion(popDto.getCleanFileName(), popDto.getPortalShortcode());
        popDto.setVersion(newVersion);
        return createNew(popDto, context, false);
    }

    @Override
    public SiteMedia createNew(SiteMediaPopDto popDto, PortalPopulateContext context, boolean overwrite) throws IOException {
        return siteMediaService.create(popDto);
    }
}
