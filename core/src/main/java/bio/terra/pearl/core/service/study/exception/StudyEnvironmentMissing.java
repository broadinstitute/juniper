package bio.terra.pearl.core.service.study.exception;

/**
 * throw if the invariant that all studies must have the sandbox/irb/live environments is violated.
 * Extends IllegalStateException rather than NoSuchElementException because this should never happen,
 * and if it does, it's likely because study creation/deletion is in-process
 * */
public class StudyEnvironmentMissing extends IllegalStateException {
}
