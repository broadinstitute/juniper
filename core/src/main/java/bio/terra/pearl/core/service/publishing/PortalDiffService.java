package bio.terra.pearl.core.service.publishing;

import bio.terra.pearl.core.dao.publishing.PortalEnvironmentChangeRecordDao;
import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.Versioned;
import bio.terra.pearl.core.model.dashboard.AlertTrigger;
import bio.terra.pearl.core.model.dashboard.ParticipantDashboardAlert;
import bio.terra.pearl.core.model.notification.EmailTemplate;
import bio.terra.pearl.core.model.notification.Trigger;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.portal.PortalEnvironmentLanguage;
import bio.terra.pearl.core.model.publishing.*;
import bio.terra.pearl.core.model.site.SiteContent;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.survey.StudyEnvironmentSurvey;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.service.notification.TriggerService;
import bio.terra.pearl.core.service.portal.PortalDashboardConfigService;
import bio.terra.pearl.core.service.portal.PortalEnvironmentConfigService;
import bio.terra.pearl.core.service.portal.PortalEnvironmentLanguageService;
import bio.terra.pearl.core.service.portal.PortalEnvironmentService;
import bio.terra.pearl.core.service.site.SiteContentService;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.study.StudyService;
import bio.terra.pearl.core.service.survey.SurveyService;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.beans.IntrospectionException;
import java.util.*;

import org.springframework.stereotype.Service;

@Service
public class PortalDiffService {
    public static final List<String> CONFIG_IGNORE_PROPS = List.of("id", "createdAt", "lastUpdatedAt", "class",
            "studyEnvironmentId", "portalEnvironmentId", "emailTemplateId", "emailTemplate",
            "consentFormId", "consentForm", "surveyId", "survey", "versionedEntity", "trigger");
    private final PortalEnvironmentService portalEnvService;
    private final PortalEnvironmentConfigService portalEnvironmentConfigService;
    private final SiteContentService siteContentService;
    private final SurveyService surveyService;
    private final TriggerService triggerService;
    private final PortalDashboardConfigService portalDashboardConfigService;
    private final ObjectMapper objectMapper;
    private final PortalEnvironmentChangeRecordDao portalEnvironmentChangeRecordDao;
    private final StudyEnvironmentService studyEnvironmentService;
    private final StudyService studyService;
    private final PortalEnvironmentLanguageService portalEnvironmentLanguageService;

    public PortalDiffService(PortalEnvironmentService portalEnvService,
                             PortalEnvironmentConfigService portalEnvironmentConfigService,
                             SiteContentService siteContentService, SurveyService surveyService,
                             TriggerService triggerService,
                             PortalDashboardConfigService portalDashboardConfigService,
                             ObjectMapper objectMapper,
                             PortalEnvironmentChangeRecordDao portalEnvironmentChangeRecordDao,
                             StudyEnvironmentService studyEnvironmentService, StudyService studyService,
                             PortalEnvironmentLanguageService portalEnvironmentLanguageService) {
        this.portalEnvService = portalEnvService;
        this.portalEnvironmentConfigService = portalEnvironmentConfigService;
        this.siteContentService = siteContentService;
        this.surveyService = surveyService;
        this.triggerService = triggerService;
        this.portalDashboardConfigService = portalDashboardConfigService;
        this.objectMapper = objectMapper;
        this.portalEnvironmentChangeRecordDao = portalEnvironmentChangeRecordDao;
        this.studyEnvironmentService = studyEnvironmentService;
        this.studyService = studyService;
        this.portalEnvironmentLanguageService = portalEnvironmentLanguageService;
    }

    public PortalEnvironmentChange diffPortalEnvs(String shortcode, EnvironmentName source, EnvironmentName dest) throws Exception {
        PortalEnvironment sourceEnv = loadPortalEnvForProcessing(shortcode, source);
        PortalEnvironment destEnv = loadPortalEnvForProcessing(shortcode, dest);
        return diffPortalEnvs(sourceEnv, destEnv);
    }

    public PortalEnvironmentChange diffPortalEnvs(PortalEnvironment sourceEnv, PortalEnvironment destEnv) throws Exception {
        VersionedEntityChange<Survey> preRegRecord = new VersionedEntityChange<Survey>(sourceEnv.getPreRegSurvey(), destEnv.getPreRegSurvey());
        VersionedEntityChange<SiteContent> siteContentRecord = new VersionedEntityChange<SiteContent>(sourceEnv.getSiteContent(), destEnv.getSiteContent());
        List<ConfigChange> envConfigChanges = ConfigChange.allChanges(sourceEnv.getPortalEnvironmentConfig(),
                destEnv.getPortalEnvironmentConfig(), CONFIG_IGNORE_PROPS);
        ListChange<Trigger, VersionedConfigChange<EmailTemplate>> triggerChanges = diffConfigLists(sourceEnv.getTriggers(),
                destEnv.getTriggers(),
                CONFIG_IGNORE_PROPS);

        List<StudyEnvironmentChange> studyEnvChanges = new ArrayList<>();
        List<Study> studies = studyService.findByPortalId(sourceEnv.getPortalId());
        for (Study study : studies) {
            StudyEnvironmentChange studyEnvChange = diffStudyEnvs(study.getShortcode(), sourceEnv.getEnvironmentName(), destEnv.getEnvironmentName());
            studyEnvChanges.add(studyEnvChange);
        }

        List<ParticipantDashboardAlert> destAlerts = new ArrayList<>(destEnv.getParticipantDashboardAlerts());
        List<ParticipantDashboardAlert> sourceAlerts = new ArrayList<>(sourceEnv.getParticipantDashboardAlerts());
        List<ParticipantDashboardAlertChange> alertChangeLists = diffAlertLists(sourceAlerts, destAlerts);
        ListChange<PortalEnvironmentLanguage, Object> languageChanges = diffLanguages(sourceEnv.getSupportedLanguages(), destEnv.getSupportedLanguages());
        return new PortalEnvironmentChange(
                siteContentRecord,
                envConfigChanges,
                preRegRecord,
                triggerChanges,
                alertChangeLists,
                studyEnvChanges,
                languageChanges
        );
    }

    protected List<ParticipantDashboardAlertChange> diffAlertLists(
            List<ParticipantDashboardAlert> sourceAlerts,
            List<ParticipantDashboardAlert> destAlerts) throws ReflectiveOperationException, IntrospectionException {
        Map<AlertTrigger, ParticipantDashboardAlert> unmatchedDestAlerts = new HashMap<>();
        for (ParticipantDashboardAlert destAlert : destAlerts) {
            unmatchedDestAlerts.put(destAlert.getTrigger(), destAlert);
        }

        List<ParticipantDashboardAlertChange> alertChangeLists = new ArrayList<>();
        for (ParticipantDashboardAlert sourceAlert : sourceAlerts) {
            ParticipantDashboardAlert matchedAlert = unmatchedDestAlerts.get(sourceAlert.getTrigger());
            if (matchedAlert == null) {
                List<ConfigChange> newAlert = ConfigChange.allChanges(sourceAlert, null, CONFIG_IGNORE_PROPS);
                alertChangeLists.add(new ParticipantDashboardAlertChange(sourceAlert.getTrigger(), newAlert));
            } else {
                unmatchedDestAlerts.remove(matchedAlert.getTrigger());
                List<ConfigChange> alertChanges = ConfigChange.allChanges(sourceAlert, matchedAlert, CONFIG_IGNORE_PROPS);
                if(!alertChanges.isEmpty()) {
                    alertChangeLists.add(new ParticipantDashboardAlertChange(sourceAlert.getTrigger(), alertChanges));
                }
            }
        }

        return alertChangeLists;
    }

    protected PortalEnvironment loadPortalEnvForProcessing(String shortcode, EnvironmentName envName) {
        PortalEnvironment portalEnv = portalEnvService.findOne(shortcode, envName).get();
        if (portalEnv.getPortalEnvironmentConfigId() != null) {
            portalEnv.setPortalEnvironmentConfig(portalEnvironmentConfigService
                    .find(portalEnv.getPortalEnvironmentConfigId()).get());
        }
        if (portalEnv.getSiteContentId() != null) {
            portalEnv.setSiteContent(siteContentService.find(portalEnv.getSiteContentId()).get());
        }
        if (portalEnv.getPreRegSurveyId() != null) {
            portalEnv.setPreRegSurvey(surveyService.find(portalEnv.getPreRegSurveyId()).get());
        }
        portalEnv.setSupportedLanguages(portalEnvironmentLanguageService.findByPortalEnvId(portalEnv.getId()));
        List<Trigger> triggers = triggerService.findByPortalEnvironmentId(portalEnv.getId());
        triggerService.attachTemplates(triggers);
        portalEnv.setTriggers(triggers);

        List<ParticipantDashboardAlert> alerts = portalDashboardConfigService.findByPortalEnvId(portalEnv.getId());
        portalEnv.setParticipantDashboardAlerts(alerts);

        return portalEnv;
    }

    public static <C extends VersionedEntityConfig, T extends BaseEntity & Versioned> ListChange<C, VersionedConfigChange<T>> diffConfigLists(
            List<C> sourceConfigs,
            List<C> destConfigs,
            List<String> ignoreProps)
    throws Exception {
        List<C> unmatchedDestConfigs = new ArrayList<>(destConfigs);
        List<VersionedConfigChange<T>> changedRecords = new ArrayList<>();
        List<C> addedConfigs = new ArrayList<>();
        for (C sourceConfig : sourceConfigs) {
            C matchedConfig = unmatchedDestConfigs.stream().filter(
                    destConfig -> isVersionedConfigMatch(sourceConfig, destConfig))
                    .findAny().orElse(null);
            if (matchedConfig == null) {
                addedConfigs.add(sourceConfig);
            } else {
                // this remove only works if the config has an ID, since that's how BaseEntity equality works
                // that's fine, since we're only working with already-persisted entities in this list.
                unmatchedDestConfigs.remove(matchedConfig);
                VersionedConfigChange<T> changeRecord = new VersionedConfigChange<T>(
                        sourceConfig.getId(), matchedConfig.getId(),
                        ConfigChange.allChanges(sourceConfig, matchedConfig, ignoreProps),
                        new VersionedEntityChange<T>(sourceConfig.versionedEntity(), matchedConfig.versionedEntity())
                );
                if (changeRecord.isChanged()) {
                    changedRecords.add(changeRecord);
                }

            }
        }
        return new ListChange<>(addedConfigs, unmatchedDestConfigs, changedRecords);
    }

    /** for now, just checks to see if they reference the same versioned document */
    public static boolean isVersionedConfigMatch(VersionedEntityConfig configA, VersionedEntityConfig configB) {
        if (configA == null || configB == null) {
            return configA == configB;
        }
        if (configA.versionedEntity() == null || configB.versionedEntity() == null) {
            return false;
        }
        return Objects.equals(configA.versionedEntity().getStableId(), configB.versionedEntity().getStableId());
    }

    public StudyEnvironmentChange diffStudyEnvs(String studyShortcode, EnvironmentName source, EnvironmentName dest) throws Exception {
        StudyEnvironment sourceEnv = loadStudyEnvForProcessing(studyShortcode, source);
        StudyEnvironment destEnv = loadStudyEnvForProcessing(studyShortcode, dest);
        return diffStudyEnvs(studyShortcode, sourceEnv, destEnv);
    }

    public StudyEnvironmentChange diffStudyEnvs(String studyShortcode, StudyEnvironment sourceEnv, StudyEnvironment destEnv) throws Exception {
        List<ConfigChange> envConfigChanges = ConfigChange.allChanges(
                sourceEnv.getStudyEnvironmentConfig(),
                destEnv.getStudyEnvironmentConfig(),
                CONFIG_IGNORE_PROPS);
        VersionedEntityChange<Survey> preEnrollChange = new VersionedEntityChange<Survey>(sourceEnv.getPreEnrollSurvey(), destEnv.getPreEnrollSurvey());
        ListChange<StudyEnvironmentSurvey, VersionedConfigChange<Survey>> surveyChanges = diffConfigLists(
                sourceEnv.getConfiguredSurveys(),
                destEnv.getConfiguredSurveys(),
                CONFIG_IGNORE_PROPS);
        ListChange<Trigger, VersionedConfigChange<EmailTemplate>> triggerChanges = diffConfigLists(
                sourceEnv.getTriggers(),
                destEnv.getTriggers(),
                CONFIG_IGNORE_PROPS);

        return new StudyEnvironmentChange(
                studyShortcode,
                envConfigChanges,
                preEnrollChange,
                surveyChanges,
                triggerChanges
        );
    }

    /** diffs the two lists -- any changes to a language will be considered an add/remove */
    public ListChange<PortalEnvironmentLanguage, Object> diffLanguages(List<PortalEnvironmentLanguage> sourceLangs, List<PortalEnvironmentLanguage> destLangs) {
        List<PortalEnvironmentLanguage> unmatchedDestLangs = new ArrayList<>(destLangs);
        List<PortalEnvironmentLanguage> addedLangs = new ArrayList<>();
        for (PortalEnvironmentLanguage sourceLang : sourceLangs) {
            PortalEnvironmentLanguage matchedLang = unmatchedDestLangs.stream().filter(
                    destLang -> destLang.getLanguageCode().equals(sourceLang.getLanguageCode()) && destLang.getLanguageName().equals(sourceLang.getLanguageName()))
                    .findAny().orElse(null);
            if (matchedLang == null) {
                addedLangs.add(sourceLang);
            } else {
                unmatchedDestLangs.remove(matchedLang);
            }
        }
        return new ListChange<>(addedLangs, unmatchedDestLangs, Collections.emptyList());
    }

    public StudyEnvironment loadStudyEnvForProcessing(String shortcode, EnvironmentName envName) {
        StudyEnvironment studyEnvironment = studyEnvironmentService.findByStudy(shortcode, envName).get();
        return studyEnvironmentService.loadWithAllContent(studyEnvironment);
    }


}
