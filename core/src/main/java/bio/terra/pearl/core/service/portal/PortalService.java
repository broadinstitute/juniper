package bio.terra.pearl.core.service.portal;

import bio.terra.pearl.core.dao.admin.PortalAdminUserDao;
import bio.terra.pearl.core.dao.portal.PortalDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.study.PortalStudy;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.CrudService;
import bio.terra.pearl.core.service.consent.ConsentFormService;
import bio.terra.pearl.core.service.exception.PermissionDeniedException;
import bio.terra.pearl.core.service.notification.EmailTemplateService;
import bio.terra.pearl.core.service.participant.ParticipantUserService;
import bio.terra.pearl.core.service.participant.PortalParticipantUserService;
import bio.terra.pearl.core.service.site.SiteContentService;
import bio.terra.pearl.core.service.site.SiteImageService;
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
    private PortalParticipantUserService portalParticipantUserService;
    private PortalAdminUserDao portalAdminUserDao;
    private StudyService studyService;
    private SurveyService surveyService;
    private ConsentFormService consentFormService;
    private SiteContentService siteContentService;
    private EmailTemplateService emailTemplateService;
    private SiteImageService siteImageService;

    public PortalService(PortalDao portalDao, PortalStudyService portalStudyService,
                         StudyService studyService,
                         PortalEnvironmentService portalEnvironmentService,
                         ParticipantUserService participantUserService,
                         PortalParticipantUserService portalParticipantUserService,
                         PortalAdminUserDao portalAdminUserDao, SurveyService surveyService,
                         ConsentFormService consentFormService, SiteContentService siteContentService,
                         EmailTemplateService emailTemplateService,
                         SiteImageService siteImageService) {
        super(portalDao);
        this.portalStudyService = portalStudyService;
        this.portalEnvironmentService = portalEnvironmentService;
        this.studyService = studyService;
        this.participantUserService = participantUserService;
        this.portalParticipantUserService = portalParticipantUserService;
        this.portalAdminUserDao = portalAdminUserDao;
        this.surveyService = surveyService;
        this.consentFormService = consentFormService;
        this.siteContentService = siteContentService;
        this.emailTemplateService = emailTemplateService;
        this.siteImageService = siteImageService;
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
        Portal portal = dao.find(portalId).get();
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
        siteContentService.deleteByPortalId(portalId);
        emailTemplateService.deleteByPortalId(portalId);
        siteImageService.deleteByPortalShortcode(portal.getShortcode());
        portalAdminUserDao.deleteByPortalId(portalId);
        dao.delete(portalId);
    }

    public Optional<Portal> findOneByShortcode(String shortcode) {
        return dao.findOneByShortcode(shortcode);
    }
    public Optional<Portal> findOneByShortcodeOrHostname(String shortcodeOrHostname) {
        return dao.findOneByShortcodeOrHostname(shortcodeOrHostname);
    }

    public Portal fullLoad(Portal portal, String language) {
        return dao.fullLoad(portal, language);
    }

    /** loads a portal environment with everything needed to render the participant-facing site */
    public Optional<Portal> loadWithParticipantSiteContent(String shortcodeOrHostname,
                                                                       EnvironmentName environmentName,
                                                                       String language) {
        Optional<Portal> portalOpt = dao.findOneByShortcodeOrHostname(shortcodeOrHostname);
        portalOpt.ifPresent(portal -> {
            Optional<PortalEnvironment> portalEnv = portalEnvironmentService
                    .loadWithParticipantSiteContent(portal.getShortcode(), environmentName, language);
            portal.getPortalEnvironments().add(portalEnv.get());
            List<Study> studies = studyService.findWithPreregContent(portal.getShortcode(), environmentName);
            for (Study study : studies) {
                portal.getPortalStudies().add(
                        PortalStudy.builder().study(study).build()
                );
            }
        });
        return portalOpt;
    }

    public List<Portal> findByAdminUser(AdminUser user) {
        if (user.isSuperuser()) {
            return dao.findAll();
        }
        return dao.findByAdminUserId(user.getId());
    }

    /** this will throw permission denied even if the portal doesn't exist, to avoid leaking information */
    public Portal authAdminToPortal(AdminUser user, String portalShortcode) {
        Optional<Portal> portalOpt = findOneByShortcode(portalShortcode);
        if (portalOpt.isPresent()) {
            Portal portal = portalOpt.get();
            if (checkAdminIsInPortal(user, portal.getId())) {
                return portal;
            }
        }
        throw new PermissionDeniedException("User %s does not have permissions on portal %s"
                .formatted(user.getUsername(), portalShortcode));
    }

    public boolean checkAdminIsInPortal(AdminUser user, UUID portalId) {
        return user.isSuperuser() || portalAdminUserDao.isUserInPortal(user.getId(), portalId);
    }

    /**
     * checks if the adminUser is authorized for at least one of the given portals,
     * */
    public boolean checkAdminInAtLeastOnePortal(AdminUser user, List<UUID> portalIds) {
        for (UUID portalId : portalIds) {
            if (checkAdminIsInPortal(user, portalId)) {
                return true;
            }
        }
        return false;
    }

    public enum AllowedCascades implements CascadeProperty {
        PARTICIPANT_USER,
        STUDY,
        SITE_CONTENT,
        PORTAL_STUDY;

    }
}
