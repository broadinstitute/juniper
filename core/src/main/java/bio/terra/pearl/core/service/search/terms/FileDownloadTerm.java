package bio.terra.pearl.core.service.search.terms;

import bio.terra.pearl.core.dao.file.DownloadRecordDao;
import bio.terra.pearl.core.dao.file.ParticipantFileDao;
import bio.terra.pearl.core.model.file.ParticipantFile;
import bio.terra.pearl.core.service.search.EnrolleeSearchContext;
import bio.terra.pearl.core.service.search.sql.EnrolleeSearchQueryBuilder;

import java.util.List;

/**
 * Searches whether an enrollee has downloaded a file uploaded in response to a particular
 * survey question. Expression: {fileDownload.questionStableId} = true/false
 */
public class FileDownloadTerm extends FileTerm {
    private final ParticipantFileDao participantFileDao;
    private final DownloadRecordDao downloadRecordDao;

    public FileDownloadTerm(String questionStableId, ParticipantFileDao participantFileDao, DownloadRecordDao downloadRecordDao) {
        super(questionStableId, "dl");
        this.participantFileDao = participantFileDao;
        this.downloadRecordDao = downloadRecordDao;
    }

    @Override
    public SearchValue extract(EnrolleeSearchContext context) {
        List<ParticipantFile> files = participantFileDao.findByEnrolleeIdAndQuestionStableId(
                context.getEnrollee().getId(), questionStableId);
        for (ParticipantFile file : files) {
            if (!downloadRecordDao.findByParticipantFileId(file.getId()).isEmpty()) {
                return new SearchValue(true);
            }
        }
        return new SearchValue(false);
    }

    @Override
    public List<EnrolleeSearchQueryBuilder.JoinClause> requiredJoinClauses() {
        return List.of(
                answerJoinClause(),
                participantFileJoinClause(),
                new EnrolleeSearchQueryBuilder.JoinClause("download_record", drAlias(),
                        "%s.id = %s.participant_file_id".formatted(pfAlias(), drAlias()))
        );
    }

    @Override
    public String termClause() {
        return drAlias() + ".id IS NOT NULL";
    }

    private String drAlias() {
        return "dr_dl_" + questionStableId;
    }
}
