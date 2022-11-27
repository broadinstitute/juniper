package bio.terra.pearl.populate.service;

import bio.terra.pearl.core.model.Portal;
import bio.terra.pearl.core.model.PortalStudy;
import bio.terra.pearl.core.model.Study;
import bio.terra.pearl.core.service.CascadeTree;
import bio.terra.pearl.core.service.PortalService;
import bio.terra.pearl.core.service.PortalStudyService;
import bio.terra.pearl.populate.dto.PopulatePortalDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Optional;

@Service
public class PortalPopulator implements Populator<Portal> {
    private FilePopulateService filePopulateService;
    private PortalService portalService;
    private StudyPopulator studyPopulator;

    private PortalStudyService portalStudyService;
    private ObjectMapper objectMapper;

    public PortalPopulator(FilePopulateService filePopulateService,
                           PortalService portalService,
                           StudyPopulator studyPopulator,
                           PortalStudyService portalStudyService,
                           ObjectMapper objectMapper) {
        this.filePopulateService = filePopulateService;
        this.portalService = portalService;
        this.studyPopulator = studyPopulator;
        this.portalStudyService = portalStudyService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    @Override
    public Portal populate(String filePathName) throws IOException {
        FilePopulateConfig config = new FilePopulateConfig(filePathName);
        String portalFileString = filePopulateService.readFile(config.getRootFileName(), config);
        return populateFromString(portalFileString, config);
    }

    protected Portal populateFromString(String portalContent, FilePopulateConfig config)  throws IOException {
        PopulatePortalDto portalDto = objectMapper.readValue(portalContent, PopulatePortalDto.class);
        Optional<Portal> existingPortal = portalService.findOneByShortcode(portalDto.getShortcode());
        existingPortal.ifPresent(portal ->
            portalService.delete(portal.getId(), new CascadeTree(PortalService.AllowedCascades.STUDY))
        );
        Portal portal = portalService.create(portalDto);
        for (String studyFileName : portalDto.getPopulateStudyFiles()) {
            Study newStudy = studyPopulator.populate(studyFileName, config);
            PortalStudy portalStudy = portalStudyService.create(portal.getId(), newStudy.getId());
            portal.getPortalStudies().add(portalStudy);
            portalStudy.setStudy(newStudy);
        }
        return portal;
    }

}
