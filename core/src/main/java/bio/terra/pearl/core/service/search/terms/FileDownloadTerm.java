package bio.terra.pearl.core.service.search.terms;

import bio.terra.pearl.core.dao.file.DownloadRecordDao;
import bio.terra.pearl.core.dao.file.ParticipantFileDao;
import bio.terra.pearl.core.model.file.ParticipantFile;
import bio.terra.pearl.core.model.search.SearchValueTypeDefinition;
import bio.terra.pearl.core.service.search.EnrolleeSearchContext;
import bio.terra.pearl.core.service.search.sql.EnrolleeSearchQueryBuilder;
import org.jooq.Condition;

import java.util.List;
import java.util.Optional;

import static bio.terra.pearl.core.service.search.terms.SearchValue.SearchValueType.BOOLEAN;

/**
 * Searches whether an enrollee has downloaded a file that was uploaded in response to a
 * particular survey question. Expression: {fileDownload.questionStableId} = true/false
 * The question stableId is validated as alphanumeric+underscore so it can be safely embedded
 * in the SQL JOIN ON clauses (same approach as AnswerTerm).
 */
public class FileDownloadTerm extends SearchTerm {
    private final String questionStableId;
    private final ParticipantFileDao participantFileDao;
    private final DownloadRecordDao downloadRecordDao;

    public FileDownloadTerm(String questionStableId, ParticipantFileDao participantFileDao, DownloadRecordDao downloadRecordDao) {
        if (!isAlphaNumeric(questionStableId)) {
            throw new IllegalArgumentException("Invalid question stable id: must be alphanumeric and underscore only");
        }
        this.questionStableId = questionStableId;
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
        // SAFE: questionStableId is validated as alphanumeric+underscore in the constructor
        return List.of(
                new EnrolleeSearchQueryBuilder.JoinClause("answer", answerAlias(),
                        "enrollee.id = %s.enrollee_id AND %s.question_stable_id = '%s' AND %s.format = 'FILE_UPLOAD'"
                                .formatted(answerAlias(), answerAlias(), questionStableId, answerAlias())),
                new EnrolleeSearchQueryBuilder.JoinClause("participant_file", pfAlias(),
                        "enrollee.id = %s.enrollee_id AND %s.object_value::jsonb @> json_build_array(json_build_object('fileName', %s.file_name))::jsonb"
                                .formatted(pfAlias(), answerAlias(), pfAlias())),
                new EnrolleeSearchQueryBuilder.JoinClause("download_record", drAlias(),
                        "%s.id = %s.participant_file_id".formatted(pfAlias(), drAlias()))
        );
    }

    @Override
    public List<EnrolleeSearchQueryBuilder.SelectClause> requiredSelectClauses() {
        return List.of();
    }

    @Override
    public Optional<Condition> requiredConditions() {
        return Optional.empty();
    }

    @Override
    public String termClause() {
        return drAlias() + ".id IS NOT NULL";
    }

    @Override
    public List<Object> boundObjects() {
        return List.of();
    }

    @Override
    public SearchValueTypeDefinition type() {
        return SearchValueTypeDefinition.builder().type(BOOLEAN).build();
    }

    private String answerAlias() {
        return "a_dl_" + questionStableId;
    }

    private String pfAlias() {
        return "pf_dl_" + questionStableId;
    }

    private String drAlias() {
        return "dr_dl_" + questionStableId;
    }

    private static boolean isAlphaNumeric(String s) {
        return s != null && s.matches("^[a-zA-Z0-9_]+$");
    }
}
