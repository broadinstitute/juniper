package bio.terra.pearl.core.service.search.terms;

import bio.terra.pearl.core.dao.file.ParticipantFileDao;
import bio.terra.pearl.core.model.file.ParticipantFile;
import bio.terra.pearl.core.service.search.EnrolleeSearchContext;
import bio.terra.pearl.core.service.search.sql.EnrolleeSearchQueryBuilder;

import java.util.List;

/** Expression: {fileUpload.questionStableId} = true/false */
public class FileAnswerTerm extends BaseFileAnswerTerm {
    private final ParticipantFileDao participantFileDao;

    public FileAnswerTerm(String questionStableId, ParticipantFileDao participantFileDao) {
        super(questionStableId, "ul");
        this.participantFileDao = participantFileDao;
    }

    @Override
    public SearchValue extract(EnrolleeSearchContext context) {
        List<ParticipantFile> files = participantFileDao.findByEnrolleeIdAndQuestionStableId(
                context.getEnrollee().getId(), questionStableId);
        return new SearchValue(!files.isEmpty());
    }

    @Override
    public List<EnrolleeSearchQueryBuilder.JoinClause> requiredJoinClauses() {
        return List.of(answerJoinClause(), participantFileJoinClause());
    }

    @Override
    public String termClause() {
        return pfAlias() + ".id IS NOT NULL";
    }
}
