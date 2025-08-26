package bio.terra.pearl.core.service.notification;

import bio.terra.pearl.core.dao.notification.TriggerDao;
import bio.terra.pearl.core.model.notification.EmailTemplate;
import bio.terra.pearl.core.model.notification.Trigger;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.publishing.ListChange;
import bio.terra.pearl.core.model.publishing.PortalEnvironmentChange;
import bio.terra.pearl.core.model.publishing.StudyEnvironmentChange;
import bio.terra.pearl.core.model.publishing.VersionedConfigChange;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.CrudService;
import bio.terra.pearl.core.service.notification.email.EmailTemplateService;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import bio.terra.pearl.core.service.publishing.PortalEnvPublishable;
import bio.terra.pearl.core.service.publishing.PublishingUtils;
import bio.terra.pearl.core.service.publishing.StudyEnvPublishable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TriggerService extends CrudService<Trigger, TriggerDao> implements PortalEnvPublishable, StudyEnvPublishable {
    private EmailTemplateService emailTemplateService;

    public TriggerService(TriggerDao dao, EmailTemplateService emailTemplateService) {
        super(dao);
        this.emailTemplateService = emailTemplateService;
    }

    public List<Trigger> findByStudyEnvironmentId(UUID studyEnvironmentId) {
        return dao.findByStudyEnvironmentId(studyEnvironmentId);
    }

    public List<Trigger> findByStudyEnvironmentId(UUID studyEnvironmentId, boolean active) {
        return dao.findByStudyEnvironmentId(studyEnvironmentId, active);
    }

    /** gets configs unaffiliated with a study */
    public List<Trigger> findByPortalEnvironmentId(UUID portalEnvId) {
        return dao.findByPortalEnvironmentId(portalEnvId);
    }

    @Override
    public Trigger create(Trigger action) {
        EmailTemplate emailTemplate = action.getEmailTemplate();
        if (emailTemplate != null && emailTemplate.getId() == null) {
            emailTemplate = emailTemplateService.createNewVersion(emailTemplate);
            action.setEmailTemplateId(emailTemplate.getId());
        }
        Trigger savedConfig = dao.create(action);
        savedConfig.setEmailTemplate(emailTemplate);
        return savedConfig;
    }

    public void attachTemplates(List<Trigger> actions) {
        dao.attachTemplates(actions);
    }

    public void deleteByStudyEnvironmentId(UUID studyEnvironmentId) {
        dao.deleteByStudyEnvironmentId(studyEnvironmentId);
    }

    public void deleteByPortalEnvironmentId(UUID portalEnvironmentId) {
        dao.deleteByPortalEnvironmentId(portalEnvironmentId);
    }

    @Override
    public void loadForPublishing(PortalEnvironment portalEnv) {
        List<Trigger> triggers = findByPortalEnvironmentId(portalEnv.getId());
        attachTemplates(triggers);
        portalEnv.setTriggers(triggers);
    }

    @Override
    public void updateDiff(PortalEnvironmentChange change, PortalEnvironment sourceEnv, PortalEnvironment destEnv) {
        ListChange<Trigger, VersionedConfigChange<EmailTemplate>> triggerChanges = PortalEnvPublishable.diffConfigLists(
                sourceEnv.getTriggers(),
                destEnv.getTriggers(),
                getPublishIgnoreProps());
        change.setTriggerChanges(triggerChanges);
    }

    @Override
    public void loadForPublishing(StudyEnvironment studyEnv) {
        List<Trigger> triggers = findByStudyEnvironmentId(studyEnv.getId(), true);
        attachTemplates(triggers);
        studyEnv.setTriggers(triggers);
    }

    @Override
    public void updateDiff(StudyEnvironmentChange change, StudyEnvironment sourceEnv, StudyEnvironment destEnv) {
        ListChange<Trigger, VersionedConfigChange<EmailTemplate>> triggerChanges = PortalEnvPublishable.diffConfigLists(
                sourceEnv.getTriggers(),
                destEnv.getTriggers(),
                getPublishIgnoreProps());
        change.setTriggerChanges(triggerChanges);
    }

    @Override
    public void applyDiff(PortalEnvironmentChange change, PortalEnvironment destEnv) {
        ListChange<Trigger, VersionedConfigChange<EmailTemplate>> listChange = change.getTriggerChanges();
        for (Trigger config : listChange.addedItems()) {
            config.setPortalEnvironmentId(destEnv.getId());
            create(config.cleanForCopying());
            destEnv.getTriggers().add(config);
            PublishingUtils.assignPublishedVersionIfNeeded(destEnv.getEnvironmentName(), config, emailTemplateService);
        }
        for (Trigger config : listChange.removedItems()) {
            // don't delete notification configs since they may be referenced by already-sent emails
            config.setActive(false);
            update(config);
            destEnv.getTriggers().remove(config);
        }
        for (VersionedConfigChange<EmailTemplate> configChange : listChange.changedItems()) {
            PublishingUtils.applyChangesToVersionedConfig(configChange, this, emailTemplateService, destEnv.getEnvironmentName(), destEnv.getPortalId());
        }
    }

    @Override
    @Transactional
    public void applyDiff(StudyEnvironmentChange change, StudyEnvironment destEnv, PortalEnvironment destPortalEnv) {
        ListChange<Trigger, VersionedConfigChange<EmailTemplate>> listChange = change.getTriggerChanges();
        for (Trigger config : listChange.addedItems()) {
            config.setStudyEnvironmentId(destEnv.getId());
            config.setPortalEnvironmentId(destPortalEnv.getId());
            create(config.cleanForCopying());
            destEnv.getTriggers().add(config);
            PublishingUtils.assignPublishedVersionIfNeeded(destEnv.getEnvironmentName(), config, emailTemplateService);
        }
        for (Trigger config : listChange.removedItems()) {
            // don't delete notification configs since they may be referenced by already-sent emails
            config.setActive(false);
            update(config);
            destEnv.getTriggers().remove(config);
        }
        for (VersionedConfigChange<EmailTemplate> configChange : listChange.changedItems()) {
            PublishingUtils.applyChangesToVersionedConfig(configChange, this, emailTemplateService, destEnv.getEnvironmentName(), destPortalEnv.getPortalId());
        }
    }

    @Override
    public List<String> getAdditionalPublishIgnoreProps() {
        return List.of("emailTemplateId", "emailTemplate", "versionedEntity");
    }

    @Override
    /** we have to re-implement this method since this class implements both interfaces, and otherwise the default methods collide */
    public List<String> getPublishIgnoreProps() {
        return Stream.concat(DEFAULT_PUBLISH_IGNORE_PROPS.stream(), getAdditionalPublishIgnoreProps().stream()).toList();
    }
}
