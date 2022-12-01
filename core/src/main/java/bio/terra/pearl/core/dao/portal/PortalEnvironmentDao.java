package bio.terra.pearl.core.dao.portal;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import org.apache.commons.lang3.StringUtils;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class PortalEnvironmentDao extends BaseJdbiDao<PortalEnvironment> {
    public PortalEnvironmentDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    public Class<PortalEnvironment> getClazz() {
        return PortalEnvironment.class;
    }

    public List<PortalEnvironment> findByPortal(UUID portalId) {
        return findAllByProperty("portal_id", portalId);
    }

    public Optional<PortalEnvironment> findOne(String shortcode, EnvironmentName environmentName) {
        List<String> primaryCols = getQueryColumns.stream().map(col -> "a." + col)
                .collect(Collectors.toList());
        return jdbi.withHandle(handle ->
                handle.createQuery("select " + StringUtils.join(primaryCols, ", ") + " from " + tableName
                                + " a join portal on portal_id = portal.id"
                                + " where portal.shortcode = :shortcode and environment_name = :environmentName")
                        .bind("shortcode", shortcode)
                        .bind("environmentName", environmentName)
                        .mapTo(clazz)
                        .findOne()
        );
    }
}
