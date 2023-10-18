package bio.terra.pearl.core.service.kit.pepper;

/** runtime version of PepperParseException for more convenient including in lambdas */
public class PepperParseException extends RuntimeException {
    public final String responseString;
    public final Object responseObj;

    public PepperParseException(String message, String responseString) {
        this(message, responseString, null);
    }

    public PepperParseException(String message, String responseString, Object responseObj) {
        super(message);
        this.responseString = responseString;
        this.responseObj = responseObj;
    }
}
