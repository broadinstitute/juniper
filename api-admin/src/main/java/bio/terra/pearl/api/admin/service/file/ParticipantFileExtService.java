package bio.terra.pearl.api.admin.service.file;

import bio.terra.pearl.api.admin.service.auth.EnforcePortalEnrolleePermission;
import bio.terra.pearl.api.admin.service.auth.context.PortalEnrolleeAuthContext;
import bio.terra.pearl.core.dao.file.DownloadRecordDao;
import bio.terra.pearl.core.model.file.DownloadRecord;
import bio.terra.pearl.core.model.file.ParticipantFile;
import bio.terra.pearl.core.model.file.ScannedParticipantFileDto;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.file.ParticipantFileService;
import bio.terra.pearl.core.service.file.VirusScanResult;
import bio.terra.pearl.core.service.file.backends.FileStorageBackend;
import bio.terra.pearl.core.service.file.backends.FileStorageBackendProvider;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ParticipantFileExtService {

  private final ParticipantFileService participantFileService;
  private final FileStorageBackend fileStorageBackend;
  private final DownloadRecordDao downloadRecordDao;

  public ParticipantFileExtService(
      ParticipantFileService participantFileService,
      FileStorageBackendProvider fileStorageBackendProvider,
      DownloadRecordDao downloadRecordDao) {
    this.participantFileService = participantFileService;
    this.fileStorageBackend = fileStorageBackendProvider.get();
    this.downloadRecordDao = downloadRecordDao;
  }

  @Transactional
  @EnforcePortalEnrolleePermission(permission = "participant_data_view")
  public InputStream downloadFile(PortalEnrolleeAuthContext authContext, String fileName) {
    ScannedParticipantFileDto participantFile = get(authContext, fileName);

    if (participantFile.getVirusScanResult() == VirusScanResult.QUARANTINED) {
      throw new IllegalArgumentException("Virus detected in file");
    }

    InputStream fileStream = fileStorageBackend.downloadFile(participantFile.getExternalFileId());
    downloadRecordDao.create(
        DownloadRecord.builder()
            .participantFileId(participantFile.getId())
            .adminUserId(authContext.getOperator().getId())
            .enrolleeId(authContext.getEnrollee().getId())
            .build());
    return fileStream;
  }

  @EnforcePortalEnrolleePermission(permission = "participant_data_view")
  public ScannedParticipantFileDto get(PortalEnrolleeAuthContext authContext, String fileName) {
    ParticipantFile file =
        participantFileService
            .findByEnrolleeIdAndFileName(authContext.getEnrollee().getId(), fileName)
            .orElseThrow(() -> new NotFoundException("File not found"));
    participantFileService.attachDownloadRecords(List.of(file));
    return participantFileService.attachVirusScanResult(file);
  }

  @EnforcePortalEnrolleePermission(permission = "participant_data_view")
  public List<ScannedParticipantFileDto> list(PortalEnrolleeAuthContext authContext) {
    List<ParticipantFile> files =
        participantFileService.findByEnrolleeId(authContext.getEnrollee().getId());
    participantFileService.attachDownloadRecords(files);
    return files.stream().map(participantFileService::attachVirusScanResult).toList();
  }

  @EnforcePortalEnrolleePermission(permission = "participant_data_edit")
  public ScannedParticipantFileDto uploadFile(
      PortalEnrolleeAuthContext authContext, MultipartFile file) {
    try {
      return new ScannedParticipantFileDto(
          participantFileService.uploadFileAndCreate(
              ParticipantFile.builder()
                  .enrolleeId(authContext.getEnrollee().getId())
                  .fileName(getFileName(file.getOriginalFilename()))
                  .fileType(file.getContentType())
                  .build(),
              file.getInputStream()),
          VirusScanResult.UNSCANNED);
    } catch (IOException e) {
      throw new RuntimeException("Error uploading file");
    }
  }

  // Returns the name of the file without the preceding path
  public String getFileName(String fileName) {
    if (fileName == null) {
      return "";
    }
    String[] split = fileName.split("\\[/\\\\]");
    return split[split.length - 1];
  }
}
