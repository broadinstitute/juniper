package bio.terra.pearl.api.admin.controller.file;

import bio.terra.common.exception.UnauthorizedException;
import bio.terra.pearl.api.admin.api.ParticipantFileApi;
import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.api.admin.service.auth.context.PortalEnrolleeAuthContext;
import bio.terra.pearl.api.admin.service.file.ParticipantFileExtService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.file.ScannedParticipantFileDto;
import bio.terra.pearl.core.service.file.VirusScanResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.util.List;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class ParticipantFileController implements ParticipantFileApi {
  private final ParticipantFileExtService participantFileExtService;
  private final HttpServletRequest request;
  private final ObjectMapper objectMapper;
  private final AuthUtilService authUtilService;

  public ParticipantFileController(
      ParticipantFileExtService participantFileExtService,
      HttpServletRequest request,
      ObjectMapper objectMapper,
      AuthUtilService authUtilService) {
    this.participantFileExtService = participantFileExtService;
    this.request = request;
    this.objectMapper = objectMapper;
    this.authUtilService = authUtilService;
  }

  @Override
  public ResponseEntity<Resource> download(
      String portalShortcode,
      String studyShortcode,
      String envName,
      String enrolleeShortcode,
      String fileName) {
    AdminUser adminUser = authUtilService.requireAdminUser(request);
    PortalEnrolleeAuthContext authContext =
        PortalEnrolleeAuthContext.of(
            adminUser,
            portalShortcode,
            studyShortcode,
            EnvironmentName.valueOf(envName),
            enrolleeShortcode);

    ScannedParticipantFileDto participantFile =
        participantFileExtService.get(authContext, fileName);

    if (participantFile.getVirusScanResult() == VirusScanResult.QUARANTINED) {
      // This will return a 403 Forbidden
      throw new UnauthorizedException("Virus detected in file");
    }

    InputStream content = participantFileExtService.downloadFile(authContext, fileName);

    MediaType mediaType;
    try {
      mediaType = MediaType.parseMediaType(participantFile.getFileType());
    } catch (Exception e) {
      mediaType = MediaType.APPLICATION_OCTET_STREAM;
    }

    return ResponseEntity.ok().contentType(mediaType).body(new InputStreamResource(content));
  }

  @Override
  public ResponseEntity<Object> list(
      String portalShortcode, String envName, String studyShortcode, String enrolleeShortcode) {
    AdminUser adminUser = authUtilService.requireAdminUser(request);
    PortalEnrolleeAuthContext authContext =
        PortalEnrolleeAuthContext.of(
            adminUser,
            portalShortcode,
            studyShortcode,
            EnvironmentName.valueOf(envName),
            enrolleeShortcode);

    List<ScannedParticipantFileDto> participantFiles = participantFileExtService.list(authContext);
    return ResponseEntity.ok(participantFiles);
  }

  @Override
  public ResponseEntity<Object> upload(
      String portalShortcode,
      String envName,
      String studyShortcode,
      String enrolleeShortcode,
      MultipartFile participantFile) {
    AdminUser adminUser = authUtilService.requireAdminUser(request);
    PortalEnrolleeAuthContext authContext =
        PortalEnrolleeAuthContext.of(
            adminUser,
            portalShortcode,
            studyShortcode,
            EnvironmentName.valueOf(envName),
            enrolleeShortcode);

    ScannedParticipantFileDto created =
        participantFileExtService.uploadFile(authContext, participantFile);

    return ResponseEntity.ok(created);
  }
}
