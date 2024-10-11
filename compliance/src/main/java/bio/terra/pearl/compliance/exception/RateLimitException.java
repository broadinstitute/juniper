package bio.terra.pearl.compliance.exception;

import java.time.Duration;

/**
 * Exception thrown when we hit vanta rate limits
 */
public class RateLimitException extends RuntimeException {

    private final int retryAfterDelay;

    public RateLimitException(String message, int retryAfterDelay) {
        super(message);
        this.retryAfterDelay = retryAfterDelay;
    }

    public Duration getRetryAfterDelayDuration() {
        return Duration.ofSeconds(retryAfterDelay);
    }
}