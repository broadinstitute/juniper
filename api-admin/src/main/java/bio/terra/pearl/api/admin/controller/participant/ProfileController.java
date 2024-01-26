package bio.terra.pearl.api.admin.controller.participant;

import bio.terra.pearl.api.admin.api.ProfileApi;
import bio.terra.pearl.api.admin.models.dto.ProfileUpdateDto;
import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.api.admin.service.participant.ProfileExtService;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.participant.Profile;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
public class ProfileController implements ProfileApi {
  private AuthUtilService authUtilService;
  private ProfileExtService profileExtService;
  private HttpServletRequest request;
  private ObjectMapper objectMapper;

  public ProfileController(
      AuthUtilService authUtilService,
      ProfileExtService profileExtService,
      ObjectMapper objectMapper,
      HttpServletRequest request) {
    this.authUtilService = authUtilService;
    this.profileExtService = profileExtService;
    this.objectMapper = objectMapper;
    this.request = request;
  }

  @Override
  public ResponseEntity<Object> updateProfileForEnrollee(
      String portalShortcode,
      String studyShortcode,
      String envName,
      String enrolleeShortcode,
      Object body) {
    AdminUser operator = authUtilService.requireAdminUser(request);
    ProfileUpdateDto profileUpdateDto = objectMapper.convertValue(body, ProfileUpdateDto.class);

    Profile profile =
        profileExtService.updateProfileForEnrollee(
            operator,
            enrolleeShortcode,
            profileUpdateDto.getJustification(),
            profileUpdateDto.getProfile());

    return ResponseEntity.ok(profile);
  }
}
