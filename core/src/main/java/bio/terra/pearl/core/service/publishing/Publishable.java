package bio.terra.pearl.core.service.publishing;

import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.model.Versioned;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.publishing.*;
import bio.terra.pearl.core.model.study.StudyEnvironment;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public interface Publishable {
    public static final List<String> CONFIG_IGNORE_PROPS = List.of("id", "createdAt", "lastUpdatedAt", "class",
            "studyEnvironmentId", "portalEnvironmentId", "emailTemplateId", "emailTemplate",
            "consentFormId", "consentForm", "surveyId", "survey", "versionedEntity", "trigger");
    public void loadForDiffing(PortalEnvironment portalEnv);
    public void loadForDiffing(StudyEnvironment studyEnv);
    public void updateDiff(PortalEnvironment sourceEnv, PortalEnvironment destEnv, PortalEnvironmentChange change);
    public void updateDiff(StudyEnvironment sourceEnv, StudyEnvironment destEnv, StudyEnvironmentChange change);
    public void applyDiff(PortalEnvironment sourceEnv, PortalEnvironment destEnv, PortalEnvironmentChange change);
    public void applyDiff( StudyEnvironmentChange change, StudyEnvironment destEnv, PortalEnvironment destPortalEnv);

    public static <C extends VersionedEntityConfig, T extends BaseEntity & Versioned> ListChange<C, VersionedConfigChange<T>> diffConfigLists(
            List<C> sourceConfigs,
            List<C> destConfigs,
            List<String> ignoreProps) {
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
}
