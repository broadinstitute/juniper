package bio.terra.pearl.core.service.portal;

import bio.terra.pearl.core.dao.portal.PortalDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.study.PortalStudy;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.CrudService;
import bio.terra.pearl.core.service.admin.AdminDataChangeService;
import bio.terra.pearl.core.service.admin.PortalAdminUserService;
import bio.terra.pearl.core.service.i18n.LanguageTextService;
import bio.terra.pearl.core.service.notification.email.EmailTemplateService;
import bio.terra.pearl.core.service.publishing.PortalEnvironmentChangeRecordService;
import bio.terra.pearl.core.service.site.SiteContentService;
import bio.terra.pearl.core.service.site.SiteMediaService;
import bio.terra.pearl.core.service.study.PortalStudyService;
import bio.terra.pearl.core.service.study.StudyService;
import bio.terra.pearl.core.service.survey.SurveyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PortalService extends CrudService<Portal, PortalDao> {
    private final PortalStudyService portalStudyService;
    private final PortalEnvironmentService portalEnvironmentService;
    private final PortalAdminUserService portalAdminUserService;
    private final StudyService studyService;
    private final SurveyService surveyService;
    private final SiteContentService siteContentService;
    private final EmailTemplateService emailTemplateService;
    private final SiteMediaService siteMediaService;
    private final LanguageTextService languageTextService;
    private final PortalEnvironmentChangeRecordService portalEnvironmentChangeRecordService;
    private final AdminDataChangeService adminDataChangeService;

    public PortalService(PortalDao portalDao, PortalStudyService portalStudyService,
                         PortalAdminUserService portalAdminUserService, StudyService studyService,
                         PortalEnvironmentService portalEnvironmentService,
                         SurveyService surveyService, SiteContentService siteContentService,
                         EmailTemplateService emailTemplateService,
                         SiteMediaService siteMediaService, LanguageTextService languageTextService,
                         PortalEnvironmentChangeRecordService portalEnvironmentChangeRecordService, AdminDataChangeService adminDataChangeService) {
        super(portalDao);
        this.portalStudyService = portalStudyService;
        this.portalAdminUserService = portalAdminUserService;
        this.portalEnvironmentService = portalEnvironmentService;
        this.studyService = studyService;
        this.surveyService = surveyService;
        this.siteContentService = siteContentService;
        this.emailTemplateService = emailTemplateService;
        this.siteMediaService = siteMediaService;
        this.languageTextService = languageTextService;
        this.portalEnvironmentChangeRecordService = portalEnvironmentChangeRecordService;
        this.adminDataChangeService = adminDataChangeService;
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
        portalEnvironmentChangeRecordService.deleteByPortalId(portalId);
        surveyService.deleteByPortalId(portalId);
        siteContentService.deleteByPortalId(portalId);
        emailTemplateService.deleteByPortalId(portalId);
        languageTextService.deleteByPortalId(portalId);
        siteMediaService.deleteByPortalShortcode(portal.getShortcode());
        DataAuditInfo auditInfo = DataAuditInfo.builder().systemProcess("PortalService.delete").build();
        portalAdminUserService.deleteByPortalId(portalId, auditInfo);
        adminDataChangeService.deleteByPortalId(portalId);
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
    public Optional<Portal> loadWithParticipantSiteContent(
            String shortcodeOrHostname,
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

    public void attachPortalEnvironments(List<Portal> portals) {
        dao.attachPortalEnvironments(portals);
    }

    public void attachStudies(List<Portal> portals) {
        dao.attachStudies(portals);
    }

    public boolean checkAdminIsInPortal(AdminUser user, UUID portalId) {
        return user.isSuperuser() || portalAdminUserService.isUserInPortal(user.getId(), portalId);
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

    public Optional<Portal> findByPortalEnvironmentId(UUID portalEnvironmentId) {
        return dao.findByPortalEnvironmentId(portalEnvironmentId);
    }

    public enum AllowedCascades implements CascadeProperty {
        PARTICIPANT_USER,
        STUDY,
        SITE_CONTENT,
        PORTAL_STUDY;

    }
}
