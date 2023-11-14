package bio.terra.pearl.core.service.study.exception;

/** throw if the invariant that all studies must have the sandbox/irb/live environments is violated */
public class StudyEnvironmentMissing extends RuntimeException {
}
