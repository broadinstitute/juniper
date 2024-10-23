package bio.terra.pearl.core.service.participant;

import bio.terra.pearl.core.dao.participant.ParticipantUserDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.CrudService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class ParticipantUserService extends CrudService<ParticipantUser, ParticipantUserDao> {
    private final PortalParticipantUserService portalParticipantUserService;
    private final ShortcodeService shortcodeService;

    public ParticipantUserService(ParticipantUserDao participantUserDao,
                                  PortalParticipantUserService portalParticipantUserService, ShortcodeService shortcodeService) {
        super(participantUserDao);
        this.portalParticipantUserService = portalParticipantUserService;
        this.shortcodeService = shortcodeService;
    }

    @Transactional
    public ParticipantUser create(ParticipantUser participantUser) {
        if (participantUser.getShortcode() == null) {
            participantUser.setShortcode(shortcodeService.generateShortcode("ACC", dao::findOneByShortcode));
        }
        ParticipantUser savedParticipantUser = dao.create(participantUser);
        logger.info("ParticipantUser created.  id: {}, shortcode: {}", savedParticipantUser.getId(),
                savedParticipantUser.getShortcode());
        return savedParticipantUser;
    }

    public Optional<ParticipantUser> findOneByShortcode(String shortcode) {
        return dao.findOneByShortcode(shortcode);
    }

    @Transactional @Override
    public void delete(UUID userId, Set<CascadeProperty> cascades) {
        portalParticipantUserService.deleteByParticipantUserId(userId);
        dao.delete(userId);
    }

    /** deletes users no longer attached to any portals */
    @Transactional
    public void deleteOrphans(List<UUID> userIds, Set<CascadeProperty> cascades) {
        userIds.stream().forEach(userId -> {
            if (portalParticipantUserService.findByParticipantUserId(userId).size() == 0) {
                delete(userId, cascades);
            }
        });
    }

    public Optional<ParticipantUser> findOne(String username, EnvironmentName environmentName) {
        return dao.findOne(username, environmentName);
    }

    public Optional<ParticipantUser> findByEnrolleeId(UUID enrolleeId) {
        return dao.findByEnrolleeId(enrolleeId);
    }

    public List<ParticipantUser> findAllByPortalEnv(UUID portalId, EnvironmentName envName) {
        return dao.findAllByPortalEnv(portalId, envName);
    }
}
