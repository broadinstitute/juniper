package bio.terra.pearl.core.service.workflow;

/**
 * To implement complex deterministic workflows via an event structure, we need strong determinism that,
 * e.g. an enrollee's consent status gets evaluated before we evaulate what surveys to give.
 * The goal of this class is to make that determinism explicit in terms of numeric ordering on the event bus,
 * rather than implicit in terms of chains of different event classes operated on by different listeners.
 *
 * All classes that listen to spring events should be given an ordered priority below, even if they
 * don't yet listen to the same events as other classes.
  */

public class DispatcherOrder {
    public static final int CONSENT_PROCESSOR = 5;
    public static final int SURVEY_TASK = 10;
    public static final int KIT_TASK = 20;
    // actions should be processed last so they can include all previous processing
    public static final int ACTION = 100;
}
