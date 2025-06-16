package bio.terra.pearl.api.participant.controller.migration;

import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.i18n.LanguageText;
import bio.terra.pearl.core.model.migration.AuthMigrationRequestDto;
import bio.terra.pearl.core.model.migration.AuthMigrationResponseDto;
import bio.terra.pearl.core.model.migration.B2cValidationError;
import bio.terra.pearl.core.service.i18n.LanguageTextService;
import bio.terra.pearl.core.service.migration.AuthMigrationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.util.Optional;

@Controller
public class AuthMigrationController implements AuthMigrationApi {

    private final AuthMigrationService authMigrationService;
    private final ObjectMapper objectMapper;
    private final LanguageTextService languageTextService;

    @Autowired
    public AuthMigrationController(
            AuthMigrationService authMigrationService, @Qualifier("objectMapper") ObjectMapper objectMapper, LanguageTextService languageTextService) {
        this.authMigrationService = authMigrationService;
        this.objectMapper = objectMapper;
        this.languageTextService = languageTextService;
    }

    @Override
    public ResponseEntity<Object> attemptLegacyLogin(
            String portalShortcode,
            String studyShortcode,
            String envName,
            Object body) {

        AuthMigrationRequestDto requestDto = objectMapper.convertValue(body, AuthMigrationRequestDto.class);

        boolean success = authMigrationService.attemptLegacyLogIn(
                portalShortcode,
                studyShortcode,
                EnvironmentName.valueOfCaseInsensitive(envName),
                requestDto.getEmail(),
                requestDto.getPassword()
        );

        if (!success) {
            // error message is displayed to users, so we
            // need to internationalize it
            Optional<LanguageText> failedLoginErrorText = languageTextService.findSystemTextByKeyAndLanguage(
                    "failedLogIn",
                    requestDto.getLanguageCode()
            );

            String failedLogin = failedLoginErrorText
                    .map(LanguageText::getText)
                    .orElse("TODO: default");

            return ResponseEntity.status(401).body(new B2cValidationError(failedLogin));
        }


        return ResponseEntity.ok(new AuthMigrationResponseDto(true, false));

    }
}
