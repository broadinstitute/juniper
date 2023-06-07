package bio.terra.pearl.core.dao.kit;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.kit.KitRequestStatus;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class KitRequestDao extends BaseMutableJdbiDao<KitRequest> {
    public KitRequestDao(Jdbi jdbi) {
        super(jdbi);
    }

    private final String BASE_QUERY_BY_STUDY =
            " select " + prefixedGetQueryColumns("kit") + " from " + tableName + " kit " +
            " join enrollee on kit.enrollee_id = enrollee.id " +
            " join study_environment on enrollee.study_environment_id = study_environment.id " +
            " where study_environment.id = :studyEnvironmentId ";

    @Override
    protected Class<KitRequest> getClazz() { return KitRequest.class; }

    public List<KitRequest> findByEnrollee(UUID enrolleeId) {
        return super.findAllByProperty("enrollee_id", enrolleeId);
    }

    /**
     * Find all kits that are not complete (or errored) for a study.
     * This represents the set of in-flight kits that we want to keep an eye on in DSM.
     */
    public List<KitRequest> findIncompleteKits(UUID studyEnvironmentId) {
        return jdbi.withHandle(handle ->
                handle.createQuery(" select " + prefixedGetQueryColumns("kit") + " from " + tableName + " kit " +
                                    " join enrollee on kit.enrollee_id = enrollee.id " +
                                    " join study_environment on enrollee.study_environment_id = study_environment.id " +
                                    " where study_environment.id = :studyEnvironmentId " +
                                    " and kit.status in (<kitStatuses>) ")
                        .bind("studyEnvironmentId", studyEnvironmentId)
                        .bindList("kitStatuses", KitRequestStatus.NON_TERMINAL_STATES)
                        .mapTo(clazz)
                        .list()
        );
    }
}
