package bio.terra.pearl.core.service.search.terms;

import bio.terra.pearl.core.dao.file.ParticipantFileDao;
import bio.terra.pearl.core.model.search.SearchValueTypeDefinition;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static bio.terra.pearl.core.service.search.terms.SearchValue.SearchValueType.BOOLEAN;

/** Base parser for file-related search terms. Provides getFacets() over FILE_UPLOAD question stable IDs. */
public abstract class FileTermParser<T extends FileTerm> extends SearchTermParser<T> {
    protected final ParticipantFileDao participantFileDao;

    protected FileTermParser(ParticipantFileDao participantFileDao) {
        this.participantFileDao = participantFileDao;
    }

    @Override
    public Map<String, SearchValueTypeDefinition> getFacets(UUID studyEnvId) {
        Map<String, SearchValueTypeDefinition> facets = new HashMap<>();
        for (String stableId : participantFileDao.findFileUploadStableIdsByStudyEnv(studyEnvId)) {
            facets.put(stableId, SearchValueTypeDefinition.builder().type(BOOLEAN).build());
        }
        return addTermPrefix(facets);
    }
}
