package bio.terra.pearl.core.service.publishing;

import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.publishing.StudyEnvironmentChange;
import bio.terra.pearl.core.model.study.StudyEnvironment;

import java.util.List;
import java.util.stream.Stream;

public interface StudyEnvPublishable {
    /** load the impacted entities onto the environment for diff or apply operations */
    void loadForPublishing(StudyEnvironment studyEnv);
    void updateDiff(StudyEnvironmentChange change, StudyEnvironment sourceEnv, StudyEnvironment destEnv);
    void applyDiff( StudyEnvironmentChange change, StudyEnvironment destEnv, PortalEnvironment destPortalEnv);

    default List<String> getPublishIgnoreProps() {
        return List.of(
                "id", "createdAt", "lastUpdatedAt", "class",
                "portalEnvironmentId", "studyEnvironmentId",
                "surveyId", "survey", "versionedEntity",
                "exportOptions", "exportOptionsId", "destinationUrl"
        );
    }
}
