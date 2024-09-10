package bio.terra.pearl.core.service.portal;

import bio.terra.pearl.core.dao.portal.PortalEnvironmentDao;
import bio.terra.pearl.core.dao.survey.PreregistrationResponseDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.notification.Trigger;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.portal.PortalEnvironmentConfig;
import bio.terra.pearl.core.model.site.SiteContent;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.CrudService;
import bio.terra.pearl.core.service.notification.TriggerService;
import bio.terra.pearl.core.service.participant.ParticipantUserService;
import bio.terra.pearl.core.service.participant.PortalParticipantUserService;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import bio.terra.pearl.core.service.site.SiteContentService;
import bio.terra.pearl.core.service.survey.SurveyService;
import bio.terra.pearl.core.service.workflow.ParticipantDataChangeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PortalEnvironmentService extends CrudService<PortalEnvironment, PortalEnvironmentDao> {
    private PortalEnvironmentConfigService portalEnvironmentConfigService;
    private PortalParticipantUserService portalParticipantUserService;
    private ParticipantUserService participantUserService;
    private PreregistrationResponseDao preregistrationResponseDao;
    private TriggerService triggerService;
    private MailingListContactService mailingListContactService;
    private ParticipantDataChangeService participantDataChangeService;
    private SiteContentService siteContentService;
    private SurveyService surveyService;
    private PortalDashboardConfigService portalDashboardConfigService;
    private PortalEnvironmentLanguageService portalEnvironmentLanguageService;

    public PortalEnvironmentService(PortalEnvironmentDao portalEnvironmentDao,
                                    PortalEnvironmentConfigService portalEnvironmentConfigService,
                                    PortalParticipantUserService portalParticipantUserService,
                                    ParticipantUserService participantUserService,
                                    PreregistrationResponseDao preregistrationResponseDao,
                                    TriggerService triggerService,
                                    MailingListContactService mailingListContactService,
                                    SiteContentService siteContentService,
                                    ParticipantDataChangeService participantDataChangeService,
                                    PortalDashboardConfigService portalDashboardConfigService,
                                    SurveyService surveyService,
                                    PortalEnvironmentLanguageService portalEnvironmentLanguageService) {
        super(portalEnvironmentDao);
        this.portalEnvironmentConfigService = portalEnvironmentConfigService;
        this.portalParticipantUserService = portalParticipantUserService;
        this.participantUserService = participantUserService;
        this.preregistrationResponseDao = preregistrationResponseDao;
        this.triggerService = triggerService;
        this.mailingListContactService = mailingListContactService;
        this.participantDataChangeService = participantDataChangeService;
        this.siteContentService = siteContentService;
        this.surveyService = surveyService;
        this.portalDashboardConfigService = portalDashboardConfigService;
        this.portalEnvironmentLanguageService = portalEnvironmentLanguageService;
    }

    public List<PortalEnvironment> findByPortal(UUID portalId) {
        return dao.findByPortalWithConfigs(portalId);
    }

    public Optional<PortalEnvironment> findOne(String portalShortcode, EnvironmentName environmentName) {
        return dao.findOne(portalShortcode, environmentName);
    }

    /** loads a portal environment with everything needed to render the participant-facing site */
    public Optional<PortalEnvironment> loadWithParticipantSiteContent(String portalShortcode,
                                                                       EnvironmentName environmentName,
                                                                       String language) {
        return dao.loadWithSiteContent(portalShortcode, environmentName, language);
    }

    /** loads a portal environment with everything needed to render the participant-facing site */
    public Optional<PortalEnvironment> loadWithEnvConfig(UUID portalEnvironmentId) {
        return dao.loadWithEnvConfig(portalEnvironmentId);
    }

    @Transactional
    @Override
    public PortalEnvironment create(PortalEnvironment portalEnvironment) {
        PortalEnvironmentConfig envConfig = portalEnvironment.getPortalEnvironmentConfig();
        if (envConfig != null) {
            envConfig = portalEnvironmentConfigService.create(envConfig);
            portalEnvironment.setPortalEnvironmentConfigId(envConfig.getId());
        }
        PortalEnvironment newEnv = dao.create(portalEnvironment);
        newEnv.setPortalEnvironmentConfig(envConfig);

        newEnv.getSupportedLanguages().forEach(supportedLanguage -> {
            supportedLanguage.setPortalEnvironmentId(newEnv.getId());
            portalEnvironmentLanguageService.create(supportedLanguage);
        });

        return newEnv;
    }

    /** gets all configuration content for the environment. */
    public PortalEnvironment attachAllContent(PortalEnvironment portalEnv) {
        if (portalEnv.getPortalEnvironmentConfigId() != null) {
            portalEnv.setPortalEnvironmentConfig(portalEnvironmentConfigService
                    .find(portalEnv.getPortalEnvironmentConfigId()).get());
        }
        if (portalEnv.getSiteContentId() != null) {
            SiteContent siteContent = siteContentService.find(portalEnv.getSiteContentId()).orElseThrow();
            // for now, only include english content
            siteContentService.attachChildContent(siteContent, "en");
            portalEnv.setSiteContent(siteContentService.find(portalEnv.getSiteContentId()).get());

        }
        if (portalEnv.getPreRegSurveyId() != null) {
            portalEnv.setPreRegSurvey(surveyService.find(portalEnv.getPreRegSurveyId()).get());
        }
        List<Trigger> triggers = triggerService.findByPortalEnvironmentId(portalEnv.getId());
        triggerService.attachTemplates(triggers);
        portalEnv.setTriggers(triggers);

        return portalEnv;
    }

    @Transactional
    @Override
    public void delete(UUID id, Set<CascadeProperty> cascades) {
        PortalEnvironment portalEnvironment = dao.find(id).get();
        UUID envConfigId = portalEnvironment.getPortalEnvironmentConfigId();
        List<UUID> participantUserIds = portalParticipantUserService
                .findByPortalEnvironmentId(id).stream().map(pUser -> pUser.getParticipantUserId())
                .collect(Collectors.toList());
        portalParticipantUserService.deleteByPortalEnvironmentId(id);
        // clean up any preregistration responses not associated with a user
        preregistrationResponseDao.deleteByPortalEnvironmentId(id);
        mailingListContactService.deleteByPortalEnvId(id);
        if (cascades.contains(PortalService.AllowedCascades.PARTICIPANT_USER)) {
            participantUserService.deleteOrphans(participantUserIds, cascades);
        }
        triggerService.deleteByPortalEnvironmentId(id);
        participantDataChangeService.deleteByPortalEnvironmentId(id);
        portalDashboardConfigService.deleteAlertsByPortalEnvId(id);
        dao.delete(id);
        portalEnvironmentConfigService.delete(envConfigId, cascades);
    }

    public enum AllowedCascades implements CascadeProperty {
        PARTICIPANT_USER
    }
}
