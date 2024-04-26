package bio.terra.pearl.core.model.dataimport;

import java.util.Optional;

public enum ImportItemStatus {
    SUCCESS,
    FAILED;

    public static ImportItemStatus valueOfCaseInsensitive(String value) {
        return ImportItemStatus.valueOf(value.toUpperCase());
    }

    public static Optional<ImportItemStatus> optionalValueOfCaseInsensitive(String value) {
        try {
            return Optional.of(valueOfCaseInsensitive(value));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

}
