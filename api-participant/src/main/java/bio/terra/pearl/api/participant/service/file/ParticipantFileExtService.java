package bio.terra.pearl.api.participant.service.file;

import bio.terra.pearl.api.participant.service.AuthUtilService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.file.ParticipantFile;
import bio.terra.pearl.core.model.file.ScannedParticipantFileDto;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.file.ParticipantFileService;
import bio.terra.pearl.core.service.file.VirusScanResult;
import bio.terra.pearl.core.service.file.backends.FileStorageBackend;
import bio.terra.pearl.core.service.file.backends.FileStorageBackendProvider;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class ParticipantFileExtService {

  private final ParticipantFileService participantFileService;
  private final AuthUtilService authUtilService;
  private final FileStorageBackend fileStorageBackend;
  private final StudyEnvironmentService studyEnvironmentService;

  public ParticipantFileExtService(
      ParticipantFileService participantFileService,
      AuthUtilService authUtilService,
      FileStorageBackendProvider fileStorageBackendProvider,
      StudyEnvironmentService studyEnvironmentService) {
    this.participantFileService = participantFileService;
    this.authUtilService = authUtilService;
    this.fileStorageBackend = fileStorageBackendProvider.get();
    this.studyEnvironmentService = studyEnvironmentService;
  }

  public ScannedParticipantFileDto get(
      String portalShortcode,
      EnvironmentName envName,
      ParticipantUser participantUser,
      String enrolleeShortcode,
      String fileName) {
    authUtilService.authParticipantToPortal(participantUser.getId(), portalShortcode, envName);
    Enrollee enrollee =
        authUtilService.authParticipantUserToEnrollee(participantUser.getId(), enrolleeShortcode);

    ParticipantFile file =
        participantFileService
            .findByEnrolleeIdAndFileName(enrollee.getId(), fileName)
            .orElseThrow(() -> new NotFoundException("Could not find file"));

    return participantFileService.attachVirusScanResult(file);
  }

  @Transactional
  public InputStream downloadFile(
      String portalShortcode,
      EnvironmentName envName,
      ParticipantUser participantUser,
      String enrolleeShortcode,
      String fileName) {
    authUtilService.authParticipantToPortal(participantUser.getId(), portalShortcode, envName);
    Enrollee enrollee =
        authUtilService.authParticipantUserToEnrollee(participantUser.getId(), enrolleeShortcode);
    ParticipantFile participantFile =
        participantFileService
            .findByEnrolleeIdAndFileName(enrollee.getId(), fileName)
            .orElseThrow(() -> new NotFoundException("Could not find file"));

    return participantFileService.downloadFile(participantFile, participantUser, enrollee);
  }

  public ScannedParticipantFileDto uploadFile(
      String portalShortcode,
      EnvironmentName envName,
      ParticipantUser participantUser,
      String enrolleeShortcode,
      MultipartFile file) {
    authUtilService.authParticipantToPortal(participantUser.getId(), portalShortcode, envName);
    Enrollee enrollee =
        authUtilService.authParticipantUserToEnrollee(participantUser.getId(), enrolleeShortcode);

    try {
      return new ScannedParticipantFileDto(
          participantFileService.uploadFileAndCreate(
              ParticipantFile.builder()
                  .enrolleeId(enrollee.getId())
                  .fileName(getFileName(file.getOriginalFilename()))
                  .fileType(file.getContentType())
                  .build(),
              file.getInputStream()),
          VirusScanResult.UNSCANNED);
    } catch (IOException e) {
      throw new RuntimeException("Error uploading file");
    }
  }

  public List<ScannedParticipantFileDto> list(
      String portalShortcode,
      String studyShortcode,
      EnvironmentName envName,
      ParticipantUser participantUser,
      String enrolleeShortcode) {
    authUtilService.authParticipantToPortal(participantUser.getId(), portalShortcode, envName);
    Enrollee enrollee =
        authUtilService.authParticipantUserToEnrollee(participantUser.getId(), enrolleeShortcode);
    StudyEnvironment studyEnv = studyEnvironmentService.verifyStudy(studyShortcode, envName);
    if (!studyEnv.getId().equals(enrollee.getStudyEnvironmentId())) {
      return List.of();
    }
    return participantFileService.findByEnrolleeId(enrollee.getId()).stream()
        .map(participantFileService::attachVirusScanResult)
        .toList();
  }

  // Returns the name of the file without the preceding path
  public String getFileName(String fileName) {
    if (fileName == null) {
      return "";
    }
    String[] split = fileName.split("\\[/\\\\]");
    return split[split.length - 1];
  }

  public void delete(
      String portalShortcode,
      EnvironmentName envName,
      ParticipantUser participantUser,
      String enrolleeShortcode,
      String fileName) {
    authUtilService.authParticipantToPortal(participantUser.getId(), portalShortcode, envName);
    Enrollee enrollee =
        authUtilService.authParticipantUserToEnrollee(participantUser.getId(), enrolleeShortcode);
    ParticipantFile participantFile =
        participantFileService
            .findByEnrolleeIdAndFileName(enrollee.getId(), fileName)
            .orElseThrow(() -> new NotFoundException("Could not find file"));

    participantFileService.delete(
        participantFile.getId(), Set.of(ParticipantFileService.AllowedCascades.ANSWER));
  }
}
