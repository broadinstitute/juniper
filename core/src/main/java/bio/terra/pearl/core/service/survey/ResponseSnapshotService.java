package bio.terra.pearl.core.service.survey;

import bio.terra.pearl.core.dao.survey.ResponseSnapshotDao;
import bio.terra.pearl.core.model.survey.ParsedSnapshot;
import bio.terra.pearl.core.model.survey.ResponseData;
import bio.terra.pearl.core.model.survey.ResponseSnapshot;
import bio.terra.pearl.core.service.CrudService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ResponseSnapshotService extends CrudService<ResponseSnapshot, ResponseSnapshotDao> {
    private ObjectMapper objectMapper;

    public ResponseSnapshotService(ResponseSnapshotDao dao, ObjectMapper objectMapper) {
        super(dao);
        this.objectMapper = objectMapper;
    }

    public ParsedSnapshot parse(ResponseSnapshot snapshot) throws IOException {
        ResponseData data = objectMapper.readValue(snapshot.getFullData(), ResponseData.class);
        ParsedSnapshot parsedSnap = ParsedSnapshot.builder()
                .creatingAdminUserId(snapshot.getCreatingAdminUserId())
                .surveyResponseId(snapshot.getSurveyResponseId())
                .creatingParticipantUserId(snapshot.getCreatingParticipantUserId())
                .parsedData(data)
                .build();
        return parsedSnap;
    }

    public List<ResponseSnapshot> findByResponseId(UUID responseId) {
        return dao.findByResponseId(responseId);
    }
}
