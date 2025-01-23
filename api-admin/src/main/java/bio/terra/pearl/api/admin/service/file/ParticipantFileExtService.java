package bio.terra.pearl.api.admin.service.file;

import bio.terra.pearl.api.admin.service.auth.EnforcePortalEnrolleePermission;
import bio.terra.pearl.api.admin.service.auth.context.PortalEnrolleeAuthContext;
import bio.terra.pearl.core.model.file.ParticipantFile;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.file.ParticipantFileService;
import bio.terra.pearl.core.service.file.backends.FileStorageBackend;
import bio.terra.pearl.core.service.file.backends.FileStorageBackendProvider;
import java.io.InputStream;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ParticipantFileExtService {

  private final ParticipantFileService participantFileService;
  private final FileStorageBackend fileStorageBackend;

  public ParticipantFileExtService(
      ParticipantFileService participantFileService,
      FileStorageBackendProvider fileStorageBackendProvider) {
    this.participantFileService = participantFileService;
    this.fileStorageBackend = fileStorageBackendProvider.get();
  }

  @EnforcePortalEnrolleePermission(permission = "participant_data_view")
  public InputStream downloadFile(PortalEnrolleeAuthContext authContext, String fileName) {
    ParticipantFile participantFile = get(authContext, fileName);

    return fileStorageBackend.downloadFile(participantFile.getExternalFileId());
  }

  @EnforcePortalEnrolleePermission(permission = "participant_data_view")
  public ParticipantFile get(PortalEnrolleeAuthContext authContext, String fileName) {
    return participantFileService
        .findByEnrolleeIdAndFileName(authContext.getEnrollee().getId(), fileName)
        .orElseThrow(() -> new NotFoundException("File not found"));
  }

  @EnforcePortalEnrolleePermission(permission = "participant_data_view")
  public List<ParticipantFile> list(PortalEnrolleeAuthContext authContext) {
    return participantFileService.findByEnrolleeId(authContext.getEnrollee().getId());
  }
}
