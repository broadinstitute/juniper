package bio.terra.pearl.core.service.search.terms;

import bio.terra.pearl.core.dao.file.DownloadRecordDao;
import bio.terra.pearl.core.dao.file.ParticipantFileDao;
import org.springframework.stereotype.Service;

@Service
public class FileDownloadTermParser extends FileTermParser<FileDownloadTerm> {
    private final DownloadRecordDao downloadRecordDao;

    public FileDownloadTermParser(ParticipantFileDao participantFileDao, DownloadRecordDao downloadRecordDao) {
        super(participantFileDao);
        this.downloadRecordDao = downloadRecordDao;
    }

    @Override
    protected FileDownloadTerm parse(String arguments) {
        return new FileDownloadTerm(arguments, participantFileDao, downloadRecordDao);
    }

    @Override
    public String getTermName() {
        return "fileDownload";
    }
}
