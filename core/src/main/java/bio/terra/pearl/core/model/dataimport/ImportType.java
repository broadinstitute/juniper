package bio.terra.pearl.core.model.dataimport;

import bio.terra.pearl.core.model.EnvironmentName;

import java.util.Optional;

public enum ImportType {
    PARTICIPANT,
    FORM;

    public static ImportType valueOfCaseInsensitive(String value) {
        return ImportType.valueOf(value.toUpperCase());
    }

    public static Optional<ImportType> optionalValueOfCaseInsensitive(String value) {
        try {
            return Optional.of(valueOfCaseInsensitive(value));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

}
