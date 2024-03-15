package bio.terra.pearl.core.model.study;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/** Dto for when it's useful to pass the shortcode with the studyEnv for logging or other purposes */
public record StudyEnvWithShortcode(String studyShortcode, StudyEnvironment studyEnv) {};
