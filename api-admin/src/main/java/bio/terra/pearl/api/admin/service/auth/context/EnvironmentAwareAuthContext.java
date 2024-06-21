package bio.terra.pearl.api.admin.service.auth.context;

import bio.terra.pearl.core.model.EnvironmentName;

public interface EnvironmentAwareAuthContext {
  public EnvironmentName getEnvironmentName();
}
