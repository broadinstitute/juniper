package bio.terra.pearl.core.service.exception;

import lombok.Getter;

import java.util.UUID;

public class StudyNotFoundException extends NotFoundException {

    @Getter
    private final UUID id;

    public StudyNotFoundException(UUID id) {
        super("Study not found: %s".formatted(id));
        this.id = id;
    }

}
