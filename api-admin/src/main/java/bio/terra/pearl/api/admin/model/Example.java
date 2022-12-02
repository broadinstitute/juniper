package bio.terra.pearl.api.admin.model;

import java.util.Objects;
import javax.annotation.Nullable;

public record Example(@Nullable Long id, String userId, String message) {
  public Example {
    Objects.requireNonNull(userId);
    Objects.requireNonNull(message);
  }

  public Example(String userId, String message) {
    this(null, userId, message);
  }
}
