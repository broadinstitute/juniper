package bio.terra.pearl.core.dao.kit;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.kit.KitRequestStatus;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class KitRequestDao extends BaseMutableJdbiDao<KitRequest> {
    public KitRequestDao(Jdbi jdbi) {
        super(jdbi);
    }

    private final String BASE_QUERY_BY_STUDY =
            " select " + prefixedGetQueryColumns("kit") + " from " + tableName + " kit "
            + " join enrollee on kit.enrollee_id = enrollee.id "
            + " join study_environment on enrollee.study_environment_id = study_environment.id "
            + " where study_environment.id = :studyEnvironmentId ";

    @Override
    protected Class<KitRequest> getClazz() { return KitRequest.class; }

    public List<KitRequest> findByEnrollee(UUID enrolleeId) {
        return super.findAllByProperty("enrollee_id", enrolleeId);
    }

    public Optional<KitRequest> findByEnrolleeAndLabel(UUID enrolleeId, String kitLabel) {
        return findByTwoProperties("enrollee_id", enrolleeId, "kit_label", kitLabel);
    }

    public Map<UUID, List<KitRequest>> findByEnrolleeIds(Collection<UUID> enrolleeIds) {
        return findAllByPropertyCollection("enrollee_id", enrolleeIds)
                .stream().collect(Collectors.groupingBy(KitRequest::getEnrolleeId, Collectors.toList()));
    }

    /**
     * Find all kits that are not complete (or errored) for a study.
     * This represents the set of in-flight kits that we want to keep an eye on in Pepper.
     */
    public List<KitRequest> findByStatus(UUID studyEnvironmentId, List<KitRequestStatus> statuses) {
        return jdbi.withHandle(handle ->
                handle.createQuery(BASE_QUERY_BY_STUDY +
                                " and kit.status in (<kitStatuses>) ")
                        .bind("studyEnvironmentId", studyEnvironmentId)
                        .bindList("kitStatuses", statuses)
                        .mapTo(clazz)
                        .list()
        );
    }

    /**
     * Find all kits for a study (environment).
     */
    public List<KitRequest> findByStudyEnvironment(UUID studyEnvironmentId) {
        return jdbi.withHandle(handle ->
                handle.createQuery(BASE_QUERY_BY_STUDY)
                        .bind("studyEnvironmentId", studyEnvironmentId)
                        .mapTo(clazz)
                        .list()
        );
    }
}
