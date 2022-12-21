package bio.terra.pearl.core.service.exception;

import lombok.Getter;

import java.util.UUID;

public class UserNotFoundException extends NotFoundException {
    @Getter
    private final UUID id;

    public UserNotFoundException(UUID id) {
        super("User not found: %s".formatted(id));
        this.id = id;
    }
}
