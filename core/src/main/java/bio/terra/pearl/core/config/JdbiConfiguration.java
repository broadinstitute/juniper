package bio.terra.pearl.core.config;

import bio.terra.pearl.core.model.ParticipantUser;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;
import org.jdbi.v3.core.spi.JdbiPlugin;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

@Configuration
public class JdbiConfiguration {
    @Bean
    public Jdbi jdbi(DataSource ds, List<JdbiPlugin> jdbiPlugins, Map<Class<?>, RowMapper<?>> rowMappers) {
        TransactionAwareDataSourceProxy proxy = new TransactionAwareDataSourceProxy(ds);
        Jdbi jdbi = Jdbi.create(proxy);
        jdbiPlugins.forEach(plugin -> jdbi.installPlugin(plugin));
        rowMappers.entrySet().forEach(entry ->
                jdbi.registerRowMapper(entry.getKey(), entry.getValue())
        );
        return jdbi;
    }

    @Bean
    Map<Class<?>, RowMapper<?>> rowMappers() {
        return Map.ofEntries(
                Map.entry(ParticipantUser.class, BeanMapper.of(ParticipantUser.class))
        );
    }
}
