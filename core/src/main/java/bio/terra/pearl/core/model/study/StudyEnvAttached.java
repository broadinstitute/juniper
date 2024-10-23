package bio.terra.pearl.core.model.study;

import java.util.UUID;

/** interfaces for classes that are attached directly to a study environment */
public interface StudyEnvAttached {
    UUID getStudyEnvironmentId();
    void setStudyEnvironmentId(UUID studyEnvironmentId);
}
