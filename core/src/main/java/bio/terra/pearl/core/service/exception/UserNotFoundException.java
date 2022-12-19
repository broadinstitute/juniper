package bio.terra.pearl.core.service.exception;

import lombok.Getter;

import java.util.UUID;

public class UserNotFoundException extends ValidationException {

    @Getter
    private final UUID id;

    public UserNotFoundException(UUID id) {
        this.id = id;
    }

    @Override
    public String getMessage() {
        return "User not found: %s".formatted(id);
    }
}
