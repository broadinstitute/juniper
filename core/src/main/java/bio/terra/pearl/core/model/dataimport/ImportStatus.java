package bio.terra.pearl.core.model.dataimport;

import java.util.Optional;

public enum ImportStatus {
    PROCESSING,
    DONE;

    public static ImportStatus valueOfCaseInsensitive(String value) {
        return ImportStatus.valueOf(value.toUpperCase());
    }

    public static Optional<ImportStatus> optionalValueOfCaseInsensitive(String value) {
        try {
            return Optional.of(valueOfCaseInsensitive(value));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

}
