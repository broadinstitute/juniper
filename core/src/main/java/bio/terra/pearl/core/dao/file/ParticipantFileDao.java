package bio.terra.pearl.core.dao.file;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.dao.survey.AnswerDao;
import bio.terra.pearl.core.model.file.DownloadRecord;
import bio.terra.pearl.core.model.file.FileAnswer;
import bio.terra.pearl.core.model.file.ParticipantFile;
import bio.terra.pearl.core.model.survey.Answer;
import bio.terra.pearl.core.model.survey.AnswerFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class ParticipantFileDao extends BaseMutableJdbiDao<ParticipantFile> {
    private final AnswerDao answerDao;
    private final DownloadRecordDao downloadRecordDao;
    private final ObjectMapper objectMapper;

    public ParticipantFileDao(Jdbi jdbi, AnswerDao answerDao, DownloadRecordDao downloadRecordDao, ObjectMapper objectMapper) {
        super(jdbi);
        this.answerDao = answerDao;
        this.downloadRecordDao = downloadRecordDao;
        this.objectMapper = objectMapper;
    }

    @Override
    protected Class<ParticipantFile> getClazz() {
        return ParticipantFile.class;
    }

    public List<ParticipantFile> findBySurveyResponseId(UUID surveyResponseId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                                SELECT DISTINCT file.* FROM %s file
                                INNER JOIN answer a ON a.survey_response_id = :surveyResponseId
                                  AND a.format = 'FILE_UPLOAD'
                                  AND a.enrollee_id = file.enrollee_id
                                INNER JOIN LATERAL jsonb_array_elements(a.object_value::jsonb) AS fa
                                  ON fa->>'fileName' = file.file_name
                                """.formatted(tableName))
                        .bind("surveyResponseId", surveyResponseId)
                        .mapTo(clazz)
                        .list()
        );
    }

    public List<ParticipantFile> findByEnrolleeIdWithAnswers(UUID enrolleeId) {
        List<ParticipantFile> participantFiles = findByEnrolleeId(enrolleeId);
        List<Answer> answers = answerDao.findByEnrolleeIdAndAnswerFormat(enrolleeId, AnswerFormat.FILE_UPLOAD);

        Map<String, List<Answer>> answersByFileName = new HashMap<>();
        for (Answer answer : answers) {
            if (answer.getObjectValue() == null) continue;
            try {
                FileAnswer[] fileAnswers = objectMapper.readValue(answer.getObjectValue(), FileAnswer[].class);
                for (FileAnswer fa : fileAnswers) {
                    if (fa.getFileName() != null) {
                        answersByFileName.computeIfAbsent(fa.getFileName(), k -> new ArrayList<>()).add(answer);
                    }
                }
            } catch (Exception e) {
                // skip malformed answers
            }
        }

        for (ParticipantFile file : participantFiles) {
            file.setAssociatedAnswers(answersByFileName.getOrDefault(file.getFileName(), new ArrayList<>()));
        }

        return participantFiles;
    }

    public List<ParticipantFile> attachDownloadRecords(List<ParticipantFile> participantFiles) {
        List<UUID> fileIds = participantFiles.stream().map(ParticipantFile::getId).toList();
        Map<UUID, List<DownloadRecord>> downloadsByFileId = fileIds.isEmpty()
                ? Map.of()
                : downloadRecordDao.findByParticipantFileIds(fileIds).stream()
                        .collect(Collectors.groupingBy(DownloadRecord::getParticipantFileId));

        for (ParticipantFile file : participantFiles) {
            file.setDownloads(downloadsByFileId.getOrDefault(file.getId(), new ArrayList<>()));
        }
        return participantFiles;
    }

    public Optional<ParticipantFile> findWithAnswers(UUID id) {
        Optional<ParticipantFile> participantFile = find(id);
        participantFile.ifPresent(file -> {
            file.setAssociatedAnswers(answerDao.findFileUploadAnswersByEnrolleeAndFileName(
                    file.getEnrolleeId(), file.getFileName()));
            file.setDownloads(downloadRecordDao.findByParticipantFileId(file.getId()));
        });
        return participantFile;
    }

    public List<ParticipantFile> findByEnrolleeId(UUID enrolleeId) {
        return findAllByProperty("enrollee_id", enrolleeId);
    }

    public void deleteByEnrolleeId(UUID enrolleeId) {
        List<UUID> fileIds = findByEnrolleeId(enrolleeId).stream().map(ParticipantFile::getId).toList();
        if (!fileIds.isEmpty()) {
            downloadRecordDao.deleteByParticipantFileIds(fileIds);
        }
        deleteByProperty("enrollee_id", enrolleeId);
    }

    public Optional<ParticipantFile> findByEnrolleeIdAndFileName(UUID enrolleeId, String fileName) {
        return findByTwoProperties("enrollee_id", enrolleeId, "file_name", fileName);
    }

    public List<ParticipantFile> findByEnrolleeIds(List<UUID> enrolleeIds) {
        if (enrolleeIds.isEmpty()) {
            return List.of();
        }
        return findAllByPropertyCollection("enrollee_id", enrolleeIds);
    }

    public Map<UUID, List<ParticipantFile>> findByEnrolleeIdsWithDownloads(List<UUID> enrolleeIds) {
        List<ParticipantFile> files = findByEnrolleeIds(enrolleeIds);
        attachDownloadRecords(files);
        return files.stream().collect(Collectors.groupingBy(ParticipantFile::getEnrolleeId));
    }

    public List<ParticipantFile> findByEnrolleeIdAndQuestionStableId(UUID enrolleeId, String questionStableId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                                SELECT DISTINCT pf.* FROM participant_file pf
                                WHERE pf.enrollee_id = :enrolleeId
                                  AND EXISTS (
                                    SELECT 1 FROM answer a
                                    WHERE a.enrollee_id = pf.enrollee_id
                                      AND a.question_stable_id = :questionStableId
                                      AND a.format = 'FILE_UPLOAD'
                                      AND a.object_value::jsonb @> json_build_array(json_build_object('fileName', pf.file_name))::jsonb
                                  )
                                """)
                        .bind("enrolleeId", enrolleeId)
                        .bind("questionStableId", questionStableId)
                        .mapTo(clazz)
                        .list()
        );
    }

    public List<String> findFileUploadStableIdsByStudyEnv(UUID studyEnvId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                                SELECT DISTINCT a.question_stable_id
                                FROM answer a
                                INNER JOIN enrollee e ON a.enrollee_id = e.id
                                WHERE e.study_environment_id = :studyEnvId
                                  AND a.format = 'FILE_UPLOAD'
                                ORDER BY a.question_stable_id
                                """)
                        .bind("studyEnvId", studyEnvId)
                        .mapTo(String.class)
                        .list()
        );
    }
}
