package bio.terra.pearl.core.service.participant.search;

import bio.terra.pearl.core.dao.participant.EnrolleeSearchDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.service.participant.search.facets.sql.SqlSearchableFacet;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class EnrolleeSearchService {
    private EnrolleeSearchDao enrolleeSearchDao;

    public EnrolleeSearchService(EnrolleeSearchDao enrolleeSearchDao) {
        this.enrolleeSearchDao = enrolleeSearchDao;
    }

    public List<Map<String, Object>> search(String studyShortcode, EnvironmentName envName,
                                            List<SqlSearchableFacet> facets) {

        return enrolleeSearchDao.search(studyShortcode, envName, facets);
    }
}
