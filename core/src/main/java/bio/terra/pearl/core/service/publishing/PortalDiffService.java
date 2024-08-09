package bio.terra.pearl.core.service.publishing;

import bio.terra.pearl.core.dao.publishing.PortalEnvironmentChangeRecordDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.dashboard.AlertTrigger;
import bio.terra.pearl.core.model.dashboard.ParticipantDashboardAlert;
import bio.terra.pearl.core.model.kit.KitType;
import bio.terra.pearl.core.model.kit.StudyEnvironmentKitType;
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
import bio.terra.pearl.core.service.kit.StudyEnvironmentKitTypeService;
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
    private final StudyEnvironmentKitTypeService studyEnvironmentKitTypeService;

    public PortalDiffService(PortalEnvironmentService portalEnvService,
                             PortalEnvironmentConfigService portalEnvironmentConfigService,
                             SiteContentService siteContentService, SurveyService surveyService,
                             TriggerService triggerService,
                             PortalDashboardConfigService portalDashboardConfigService,
                             ObjectMapper objectMapper,
                             PortalEnvironmentChangeRecordDao portalEnvironmentChangeRecordDao,
                             StudyEnvironmentService studyEnvironmentService, StudyService studyService,
                             PortalEnvironmentLanguageService portalEnvironmentLanguageService, StudyEnvironmentKitTypeService studyEnvironmentKitTypeService) {
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
        this.studyEnvironmentKitTypeService = studyEnvironmentKitTypeService;
    }

    public PortalEnvironmentChange diffPortalEnvs(String shortcode, EnvironmentName source, EnvironmentName dest) {
        PortalEnvironment sourceEnv = loadPortalEnvForProcessing(shortcode, source);
        PortalEnvironment destEnv = loadPortalEnvForProcessing(shortcode, dest);
        return diffPortalEnvs(sourceEnv, destEnv);
    }

    public PortalEnvironmentChange diffPortalEnvs(PortalEnvironment sourceEnv, PortalEnvironment destEnv) {
        VersionedEntityChange<Survey> preRegRecord = new VersionedEntityChange<Survey>(sourceEnv.getPreRegSurvey(), destEnv.getPreRegSurvey());
        VersionedEntityChange<SiteContent> siteContentRecord = new VersionedEntityChange<SiteContent>(sourceEnv.getSiteContent(), destEnv.getSiteContent());
        List<ConfigChange> envConfigChanges = ConfigChange.allChanges(sourceEnv.getPortalEnvironmentConfig(),
                destEnv.getPortalEnvironmentConfig(), Publishable.CONFIG_IGNORE_PROPS);
        ListChange<Trigger, VersionedConfigChange<EmailTemplate>> triggerChanges = Publishable.diffConfigLists(sourceEnv.getTriggers(),
                destEnv.getTriggers(),
                Publishable.CONFIG_IGNORE_PROPS);

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

        return PortalEnvironmentChange.builder()
                .siteContentChange(siteContentRecord)
                .configChanges(envConfigChanges)
                .preRegSurveyChanges(preRegRecord)
                .triggerChanges(triggerChanges)
                .participantDashboardAlertChanges(alertChangeLists)
                .studyEnvChanges(studyEnvChanges)
                .languageChanges(languageChanges)
                .build();
    }

    protected List<ParticipantDashboardAlertChange> diffAlertLists(
            List<ParticipantDashboardAlert> sourceAlerts,
            List<ParticipantDashboardAlert> destAlerts) {
        Map<AlertTrigger, ParticipantDashboardAlert> unmatchedDestAlerts = new HashMap<>();
        for (ParticipantDashboardAlert destAlert : destAlerts) {
            unmatchedDestAlerts.put(destAlert.getTrigger(), destAlert);
        }

        List<ParticipantDashboardAlertChange> alertChangeLists = new ArrayList<>();
        for (ParticipantDashboardAlert sourceAlert : sourceAlerts) {
            ParticipantDashboardAlert matchedAlert = unmatchedDestAlerts.get(sourceAlert.getTrigger());
            if (matchedAlert == null) {
                List<ConfigChange> newAlert = ConfigChange.allChanges(sourceAlert, null, Publishable.CONFIG_IGNORE_PROPS);
                alertChangeLists.add(new ParticipantDashboardAlertChange(sourceAlert.getTrigger(), newAlert));
            } else {
                unmatchedDestAlerts.remove(matchedAlert.getTrigger());
                List<ConfigChange> alertChanges = ConfigChange.allChanges(sourceAlert, matchedAlert, Publishable.CONFIG_IGNORE_PROPS);
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
        triggerService.loadForDiffing(portalEnv);
        portalEnv.setSupportedLanguages(portalEnvironmentLanguageService.findByPortalEnvId(portalEnv.getId()));

        List<ParticipantDashboardAlert> alerts = portalDashboardConfigService.findByPortalEnvId(portalEnv.getId());
        portalEnv.setParticipantDashboardAlerts(alerts);

        return portalEnv;
    }


    public StudyEnvironmentChange diffStudyEnvs(String studyShortcode, EnvironmentName source, EnvironmentName dest) {
        StudyEnvironment sourceEnv = loadStudyEnvForProcessing(studyShortcode, source);
        StudyEnvironment destEnv = loadStudyEnvForProcessing(studyShortcode, dest);
        return diffStudyEnvs(studyShortcode, sourceEnv, destEnv);
    }

    public StudyEnvironmentChange diffStudyEnvs(String studyShortcode, StudyEnvironment sourceEnv, StudyEnvironment destEnv) {
        List<ConfigChange> envConfigChanges = ConfigChange.allChanges(
                sourceEnv.getStudyEnvironmentConfig(),
                destEnv.getStudyEnvironmentConfig(),
                Publishable.CONFIG_IGNORE_PROPS);
        VersionedEntityChange<Survey> preEnrollChange = new VersionedEntityChange<Survey>(sourceEnv.getPreEnrollSurvey(), destEnv.getPreEnrollSurvey());
        ListChange<StudyEnvironmentSurvey, VersionedConfigChange<Survey>> surveyChanges = Publishable.diffConfigLists(
                sourceEnv.getConfiguredSurveys(),
                destEnv.getConfiguredSurveys(),
                Publishable.CONFIG_IGNORE_PROPS);
        ListChange<KitType, KitType> kitTypeChanges = diffKitTypes(sourceEnv.getKitTypes(), destEnv.getKitTypes());


        StudyEnvironmentChange change = StudyEnvironmentChange.builder()
                .studyShortcode(studyShortcode)
                .configChanges(envConfigChanges)
                .preEnrollSurveyChanges(preEnrollChange)
                .kitTypeChanges(kitTypeChanges)
                .surveyChanges(surveyChanges).build();
        triggerService.updateDiff(sourceEnv, destEnv, change);
        return change;
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

    public ListChange<KitType, KitType> diffKitTypes(List<KitType> sourceKitTypes, List<KitType> destKitTypes) {
        List<KitType> unmatchedDestKitTypes = new ArrayList<>(destKitTypes);
        List<KitType> addedKitTypes = new ArrayList<>();
        for (KitType sourceKitType : sourceKitTypes) {
            KitType matchedKitType = unmatchedDestKitTypes.stream().filter(
                    destKitType -> destKitType.getName().equals(sourceKitType.getName()))
                    .findAny().orElse(null);
            if (matchedKitType == null) {
                addedKitTypes.add(sourceKitType);
            } else {
                unmatchedDestKitTypes.remove(matchedKitType);
            }
        }
        return new ListChange<>(addedKitTypes, unmatchedDestKitTypes, Collections.emptyList());
    }

    public StudyEnvironment loadStudyEnvForProcessing(String shortcode, EnvironmentName envName) {
        StudyEnvironment studyEnvironment = studyEnvironmentService.findByStudy(shortcode, envName).get();
        List<KitType> kitTypes = studyEnvironmentKitTypeService.findKitTypesByStudyEnvironmentId(studyEnvironment.getId());

        studyEnvironment.setKitTypes(kitTypes);
        return studyEnvironmentService.loadWithAllContent(studyEnvironment);
    }


}
