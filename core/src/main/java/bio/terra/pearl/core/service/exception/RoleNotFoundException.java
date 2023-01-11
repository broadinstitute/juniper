package bio.terra.pearl.core.service.exception;

import lombok.Getter;

public class RoleNotFoundException extends NotFoundException {

  @Getter
  private final String roleName;

  public RoleNotFoundException(String roleName) {
    super("Role does not exist: %s".formatted(roleName));
    this.roleName = roleName;
  }
}
