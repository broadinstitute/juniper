package bio.terra.pearl.core.dao;

import bio.terra.pearl.core.model.Environment;
import bio.terra.pearl.core.model.EnvironmentName;
import org.apache.commons.lang3.StringUtils;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class EnvironmentDao extends BaseJdbiDao<Environment> {
    @Override
    protected Class<Environment> getClazz() {
        return Environment.class;
    }

    public EnvironmentDao(Jdbi jdbi) {
        super(jdbi);
    }

    public Optional<Environment> findByName(EnvironmentName environmentName) {
        return findByProperty("name", environmentName.toString());
    }

    @Transactional
    public Environment buildPersisted(Environment.EnvironmentBuilder builder, String testName) {
        Optional<Environment> environment = findByName(builder.build().getName());
        if (environment.isPresent()) {
            return environment.get();
        }
        return create(builder.build());
    }

    @Transactional
    public Environment buildPersisted(Environment environment) {
        Optional<Environment> env = findByName(environment.getName());
        return env.isPresent()? env.get(): create(environment);
    }

    @Override
    protected String getCreateQuerySql() {
        return "insert into " + tableName + " (" + StringUtils.join(insertColumns, ", ") +") " +
                "values (" + StringUtils.join(insertFieldSymbols, ", ") + ") ON CONFLICT (name) " +
                "DO NOTHING;";
    }
}
