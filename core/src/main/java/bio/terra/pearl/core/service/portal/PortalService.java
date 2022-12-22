package bio.terra.pearl.core.service.portal;

import bio.terra.pearl.core.dao.admin.PortalAdminUserDao;
import bio.terra.pearl.core.dao.portal.PortalDao;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.CrudService;
import bio.terra.pearl.core.service.consent.ConsentFormService;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.exception.PermissionDeniedException;
import bio.terra.pearl.core.service.participant.ParticipantUserService;
import bio.terra.pearl.core.service.study.PortalStudyService;
import bio.terra.pearl.core.service.study.StudyService;
import bio.terra.pearl.core.service.survey.SurveyService;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PortalService extends CrudService<Portal, PortalDao> {
    private PortalStudyService portalStudyService;
    private PortalEnvironmentService portalEnvironmentService;
    private ParticipantUserService participantUserService;
    private PortalAdminUserDao portalAdminUserDao;
    private StudyService studyService;
    private SurveyService surveyService;
    private ConsentFormService consentFormService;

    public PortalService(PortalDao portalDao, PortalStudyService portalStudyService,
                         StudyService studyService,
                         PortalEnvironmentService portalEnvironmentService,
                         ParticipantUserService participantUserService,
                         PortalAdminUserDao portalAdminUserDao, SurveyService surveyService,
                         ConsentFormService consentFormService) {
        super(portalDao);
        this.portalStudyService = portalStudyService;
        this.portalEnvironmentService = portalEnvironmentService;
        this.studyService = studyService;
        this.participantUserService = participantUserService;
        this.portalAdminUserDao = portalAdminUserDao;
        this.surveyService = surveyService;
        this.consentFormService = consentFormService;
    }

    @Transactional
    @Override
    public Portal create(Portal portal) {
        Portal newPortal = dao.create(portal);

        portal.getPortalEnvironments().forEach(portalEnvironment -> {
            portalEnvironment.setPortalId(newPortal.getId());
            PortalEnvironment newEnv = portalEnvironmentService.create(portalEnvironment);
            newPortal.getPortalEnvironments().add(newEnv);
        });
        return newPortal;
    }

    @Transactional
    @Override
    public void delete(UUID portalId, Set<CascadeProperty> cascades) {
        List<UUID> studyIds = portalStudyService
                .findByPortalId(portalId).stream().map(portalStudy -> portalStudy.getStudyId())
                        .collect(Collectors.toList());
        portalStudyService.deleteByPortalId(portalId);
        if (cascades.contains(AllowedCascades.STUDY)) {
            studyService.deleteOrphans(studyIds, cascades);
        }

        List<PortalEnvironment> portalEnvironments = portalEnvironmentService.findByPortal(portalId);
        for (PortalEnvironment portalEnvironment : portalEnvironments) {
            portalEnvironmentService.delete(portalEnvironment.getId(), cascades);
        }
        surveyService.deleteByPortalId(portalId);
        consentFormService.deleteByPortalId(portalId);
        dao.delete(portalId);
    }

    public Optional<Portal> findOneByShortcode(String shortcode) {
        return dao.findOneByShortcode(shortcode);
    }

    public Optional<Portal> findOneByShortcodeFullLoad(String shortcode, String language) {
        return dao.findOneByShortcodeFullLoad(shortcode, language);
    }

    public List<Portal> findByAdminUserId(UUID userId) {
        return dao.findByAdminUserId(userId);
    }

    public Portal authUserToPortal(AdminUser user, String portalShortcode) {
        Optional<Portal> portalOpt = findOneByShortcode(portalShortcode);
        if (portalOpt.isEmpty()) {
            throw new NotFoundException("Portal not found: %s".formatted(portalShortcode));
        }
        Portal portal = portalOpt.get();
        if (user.getSuperuser() || portalAdminUserDao.isUserInPortal(user.getId(), portal.getId())) {
            return portal;
        }
        throw new PermissionDeniedException("User %s does not have permissions on portal %s"
                .formatted(user.getUsername(), portalShortcode));
    }

    public enum AllowedCascades implements CascadeProperty {
        PARTICIPANT_USER,
        STUDY,
        PORTAL_STUDY;

    }
}
