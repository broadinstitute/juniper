package bio.terra.pearl.core.model;

import java.util.Optional;

/** enum is lowercase to make it easier to incorporate in urls */
public enum EnvironmentName {
    sandbox,
    irb,
    live;
    public static EnvironmentName valueOfCaseInsensitive(String value) {
        return EnvironmentName.valueOf(value.toLowerCase());
    }

    public static Optional<EnvironmentName> optionalValueOfCaseInsensitive(String value) {
        try {
            return Optional.of(valueOfCaseInsensitive(value));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    public boolean isLive() {
        return this.equals(live);
    }
}
