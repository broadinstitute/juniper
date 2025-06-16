package bio.terra.pearl.core.model.migration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AuthMigrationResponseDto {
    private boolean tokenSuccess;
    private boolean migrationRequired;
}
