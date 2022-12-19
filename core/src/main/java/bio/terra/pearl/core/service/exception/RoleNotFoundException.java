package bio.terra.pearl.core.service.exception;

import lombok.Getter;

public class RoleNotFoundException extends ValidationException {

  @Getter
  private final String roleName;

  public RoleNotFoundException(String roleName) {
    this.roleName = roleName;
  }

  @Override
  public String getMessage() {
    return "Role does not exist: %s".formatted(roleName);
  }
}
