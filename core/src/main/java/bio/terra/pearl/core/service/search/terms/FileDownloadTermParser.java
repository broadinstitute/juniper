package bio.terra.pearl.core.service.search.terms;

import bio.terra.pearl.core.dao.file.DownloadRecordDao;
import bio.terra.pearl.core.dao.file.ParticipantFileDao;
import bio.terra.pearl.core.model.search.SearchValueTypeDefinition;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static bio.terra.pearl.core.service.search.terms.SearchValue.SearchValueType.BOOLEAN;

@Service
public class FileDownloadTermParser extends SearchTermParser<FileDownloadTerm> {
    private final ParticipantFileDao participantFileDao;
    private final DownloadRecordDao downloadRecordDao;

    public FileDownloadTermParser(ParticipantFileDao participantFileDao, DownloadRecordDao downloadRecordDao) {
        this.participantFileDao = participantFileDao;
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

    @Override
    public Map<String, SearchValueTypeDefinition> getFacets(UUID studyEnvId) {
        Map<String, SearchValueTypeDefinition> facets = new HashMap<>();
        List<String> stableIds = participantFileDao.findFileUploadStableIdsByStudyEnv(studyEnvId);
        for (String stableId : stableIds) {
            facets.put("fileDownload." + stableId,
                    SearchValueTypeDefinition.builder().type(BOOLEAN).build());
        }
        return facets;
    }
}
