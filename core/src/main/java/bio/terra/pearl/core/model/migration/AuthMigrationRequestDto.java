package bio.terra.pearl.core.model.migration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AuthMigrationRequestDto {
    private String email;
    private String password;
    private String languageCode;
}
