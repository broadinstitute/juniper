package bio.terra.pearl.core.service.search.terms;

import bio.terra.pearl.core.dao.file.ParticipantFileDao;
import org.springframework.stereotype.Service;

@Service
public class FileUploadTermParser extends FileTermParser<FileUploadTerm> {
    public FileUploadTermParser(ParticipantFileDao participantFileDao) {
        super(participantFileDao);
    }

    @Override
    protected FileUploadTerm parse(String arguments) {
        return new FileUploadTerm(arguments, participantFileDao);
    }

    @Override
    public String getTermName() {
        return "fileUpload";
    }
}
