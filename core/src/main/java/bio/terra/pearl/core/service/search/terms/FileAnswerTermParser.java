package bio.terra.pearl.core.service.search.terms;

import bio.terra.pearl.core.dao.file.ParticipantFileDao;
import org.springframework.stereotype.Service;

@Service
public class FileAnswerTermParser extends FileTermParser<FileAnswerTerm> {
    public FileAnswerTermParser(ParticipantFileDao participantFileDao) {
        super(participantFileDao);
    }

    @Override
    protected FileAnswerTerm parse(String arguments) {
        return new FileAnswerTerm(arguments, participantFileDao);
    }

    @Override
    public String getTermName() {
        return "fileUpload";
    }
}
