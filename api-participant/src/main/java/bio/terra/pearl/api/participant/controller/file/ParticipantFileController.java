package bio.terra.pearl.api.participant.controller.file;

import bio.terra.common.exception.UnauthorizedException;
import bio.terra.pearl.api.participant.api.ParticipantFileApi;
import bio.terra.pearl.api.participant.service.RequestUtilService;
import bio.terra.pearl.api.participant.service.file.ParticipantFileExtService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.file.ScannedParticipantFileDto;
import bio.terra.pearl.core.model.participant.ParticipantUser;
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
  private final RequestUtilService requestUtilService;

  public ParticipantFileController(
      ParticipantFileExtService participantFileExtService,
      HttpServletRequest request,
      ObjectMapper objectMapper,
      RequestUtilService requestUtilService) {
    this.participantFileExtService = participantFileExtService;
    this.request = request;
    this.objectMapper = objectMapper;
    this.requestUtilService = requestUtilService;
  }

  @Override
  public ResponseEntity<Resource> download(
      String portalShortcode,
      String envName,
      String studyShortcode,
      String enrolleeShortcode,
      String fileName) {
    ParticipantUser participantUser = requestUtilService.requireUser(request);

    ScannedParticipantFileDto participantFile =
        participantFileExtService.get(
            portalShortcode,
            EnvironmentName.valueOf(envName),
            participantUser,
            enrolleeShortcode,
            fileName);

    if (participantFile.getVirusScanResult() == VirusScanResult.QUARANTINED) {
      // This will return a 403 Forbidden
      throw new UnauthorizedException("Virus detected in file");
    }

    InputStream content =
        participantFileExtService.downloadFile(
            portalShortcode,
            EnvironmentName.valueOf(envName),
            participantUser,
            enrolleeShortcode,
            fileName);

    MediaType mediaType;
    try {
      mediaType = MediaType.parseMediaType(participantFile.getFileType());
    } catch (Exception e) {
      mediaType = MediaType.APPLICATION_OCTET_STREAM;
    }

    return ResponseEntity.ok().contentType(mediaType).body(new InputStreamResource(content));
  }

  @Override
  public ResponseEntity<Object> upload(
      String portalShortcode,
      String envName,
      String studyShortcode,
      String enrolleeShortcode,
      MultipartFile participantFile) {
    ParticipantUser participantUser = requestUtilService.requireUser(request);

    ScannedParticipantFileDto created =
        participantFileExtService.uploadFile(
            portalShortcode,
            EnvironmentName.valueOf(envName),
            participantUser,
            enrolleeShortcode,
            participantFile);

    return ResponseEntity.ok(created);
  }

  @Override
  public ResponseEntity<Object> list(
      String portalShortcode, String envName, String studyShortcode, String enrolleeShortcode) {
    ParticipantUser participantUser = requestUtilService.requireUser(request);

    List<ScannedParticipantFileDto> participantFiles =
        participantFileExtService.list(
            portalShortcode, EnvironmentName.valueOf(envName), participantUser, enrolleeShortcode);
    return ResponseEntity.ok(participantFiles);
  }

  @Override
  public ResponseEntity<Object> delete(
      String portalShortcode,
      String envName,
      String studyShortcode,
      String enrolleeShortcode,
      String fileName) {
    ParticipantUser participantUser = requestUtilService.requireUser(request);

    participantFileExtService.delete(
        portalShortcode,
        EnvironmentName.valueOf(envName),
        participantUser,
        enrolleeShortcode,
        fileName);

    return ResponseEntity.noContent().build();
  }
}
