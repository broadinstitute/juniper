package bio.terra.pearl.core.factory.fileupload;

import bio.terra.pearl.core.dao.file.DownloadRecordDao;
import bio.terra.pearl.core.model.file.DownloadRecord;
import bio.terra.pearl.core.model.file.ParticipantFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DownloadRecordFactory {
    @Autowired
    private DownloadRecordDao downloadRecordDao;

    public DownloadRecord buildPersisted(ParticipantFile file) {
        return downloadRecordDao.create(DownloadRecord.builder()
                .participantFileId(file.getId())
                .enrolleeId(file.getEnrolleeId())
                .participantUserId(file.getCreatingParticipantUserId())
                .build());
    }
}
