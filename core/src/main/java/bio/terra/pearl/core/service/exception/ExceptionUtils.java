package bio.terra.pearl.core.service.exception;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ExceptionUtils {
    /**
     * number of lines we grab when stack traces are too long, in addition to any bio.terra.pearl
     * lines
     */
    private static final int TRUNCATED_LOG_MIN_STACK_DEPTH = 5;
    /** Azure log limit is 16K, and multi-line logging doesn't appear to support json parsing
     * see
     * https://azure.microsoft.com/en-us/updates/multiline-logging/#:~:text=Customers%20are%20able%20see%20container,due%20to%20Log%20Analytics%20limits.
     */
    private static final int TRUNCATION_THRESHOLD = 14000;

    public static void truncateIfNeeded(Throwable ex) {
        if (Arrays.stream(ex.getStackTrace())
                .map(trace -> trace.toString())
                .collect(Collectors.joining("\n"))
                .length()
                > TRUNCATION_THRESHOLD) {
                        truncateExceptionTrace(ex);
        }
    }

    /** truncate an exception stack trace to only the first few lines, plus any juniper-owned code */
    public static void truncateExceptionTrace(Throwable ex) {
        ex.setStackTrace(
                IntStream.range(0, ex.getStackTrace().length)
                        .filter(
                                i ->
                                        i < TRUNCATED_LOG_MIN_STACK_DEPTH
                                                || ex.getStackTrace()[i].toString().startsWith("bio.terra.pearl"))
                        .mapToObj(i -> ex.getStackTrace()[i])
                        .toArray(StackTraceElement[]::new));
        if (ex.getCause() != null && !ex.getCause().equals(ex)) {
            truncateExceptionTrace(ex.getCause());
        }
    }


}
