package bio.terra.pearl.core.service.search.terms;

import bio.terra.pearl.core.dao.file.DownloadRecordDao;
import bio.terra.pearl.core.dao.file.ParticipantFileDao;
import org.springframework.stereotype.Service;

@Service
public class FileAnswerDownloadTermParser extends FileTermParser<FileAnswerDownloadTerm> {
    private final DownloadRecordDao downloadRecordDao;

    public FileAnswerDownloadTermParser(ParticipantFileDao participantFileDao, DownloadRecordDao downloadRecordDao) {
        super(participantFileDao);
        this.downloadRecordDao = downloadRecordDao;
    }

    @Override
    protected FileAnswerDownloadTerm parse(String arguments) {
        return new FileAnswerDownloadTerm(arguments, participantFileDao, downloadRecordDao);
    }

    @Override
    public String getTermName() {
        return "fileDownload";
    }
}
