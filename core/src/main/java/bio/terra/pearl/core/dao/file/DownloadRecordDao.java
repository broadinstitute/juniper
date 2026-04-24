package bio.terra.pearl.core.dao.file;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.file.DownloadRecord;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class DownloadRecordDao extends BaseMutableJdbiDao<DownloadRecord> {

    public DownloadRecordDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<DownloadRecord> getClazz() {
        return DownloadRecord.class;
    }

    public List<DownloadRecord> findByParticipantFileId(UUID participantFileId) {
        return findAllByProperty("participant_file_id", participantFileId);
    }

    public List<DownloadRecord> findByParticipantFileIds(List<UUID> participantFileIds) {
        return findAllByPropertyCollection("participant_file_id", participantFileIds);
    }

    public void deleteByParticipantFileId(UUID participantFileId) {
        deleteByProperty("participant_file_id", participantFileId);
    }
}
