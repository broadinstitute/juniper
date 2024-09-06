package bio.terra.pearl.compliance.compliance.exception;

import java.time.Duration;

public class RateLimitException extends Throwable {
    private int retryAfterDelay = 60;

    public RateLimitException(String message) {
        super(message);
    }

    public RateLimitException(String message, int retryAfterDelay) {
        super(message); this.retryAfterDelay = retryAfterDelay;
    }

    public int getRetryAfterDelay() {
        return retryAfterDelay;
    }

    public Duration getRetryAfterDelayDuration() {
        return Duration.ofSeconds(retryAfterDelay);
    }
}