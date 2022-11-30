package bio.terra.pearl.populate.service;

import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.study.PortalStudy;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.service.CascadeTree;
import bio.terra.pearl.core.service.participant.PortalParticipantUserService;
import bio.terra.pearl.core.service.portal.PortalService;
import bio.terra.pearl.core.service.study.PortalStudyService;
import bio.terra.pearl.populate.dto.PopulatePortalDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Optional;

@Service
public class PortalPopulator extends Populator<Portal> {

    private PortalService portalService;
    private StudyPopulator studyPopulator;

    private PortalStudyService portalStudyService;
    private ParticipantUserPopulator participantUserPopulator;
    private PortalParticipantUserService ppUserService;


    public PortalPopulator(FilePopulateService filePopulateService,
                           PortalService portalService,
                           StudyPopulator studyPopulator,
                           PortalStudyService portalStudyService,
                           ObjectMapper objectMapper,
                           ParticipantUserPopulator participantUserPopulator,
                           PortalParticipantUserService ppUserService) {
        this.participantUserPopulator = participantUserPopulator;
        this.ppUserService = ppUserService;
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

    public Portal populateFromString(String portalContent, FilePopulateConfig config) throws IOException {
        PopulatePortalDto portalDto = objectMapper.readValue(portalContent, PopulatePortalDto.class);
        Optional<Portal> existingPortal = portalService.findOneByShortcode(portalDto.getShortcode());
        existingPortal.ifPresent(portal ->
            portalService.delete(portal.getId(), new CascadeTree(PortalService.AllowedCascades.STUDY))
        );
        Portal portal = portalService.create(portalDto);
        for (String studyFileName : portalDto.getPopulateStudyFiles()) {
            populateStudy(studyFileName, config, portal);
        }
        for (String userFileName : portalDto.getParticipantUserFiles()) {
            populateParticipantUser(userFileName, config, portal);
        }
        return portal;
    }

    private void populateStudy(String studyFileName, FilePopulateConfig config, Portal portal) throws IOException {
        Study newStudy = studyPopulator.populate(studyFileName, config);
        PortalStudy portalStudy = portalStudyService.create(portal.getId(), newStudy.getId());
        portal.getPortalStudies().add(portalStudy);
        portalStudy.setStudy(newStudy);
    }

    private void populateParticipantUser(String userFileName, FilePopulateConfig config, Portal portal) throws IOException {
        ParticipantUser newUser = participantUserPopulator.populate(userFileName, config);
        PortalParticipantUser ppUser = ppUserService.create(portal.getId(), newUser.getId());
        portal.getPortalParticipantUsers().add(ppUser);
        ppUser.setParticipantUser(newUser);
    }

}
