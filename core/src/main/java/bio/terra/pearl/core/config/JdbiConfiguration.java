package bio.terra.pearl.core.config;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.spi.JdbiPlugin;
import org.jdbi.v3.core.statement.Slf4JSqlLogger;
import org.jdbi.v3.postgres.PostgresPlugin;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;

/** Adapted from https://deinum.biz/2018-08-13-Use-JDBI-With-Spring-Boot/ */
@Configuration
public class JdbiConfiguration {
    @Bean
    public Jdbi jdbi(DataSource ds, List<JdbiPlugin> jdbiPlugins) {
        TransactionAwareDataSourceProxy proxy = new TransactionAwareDataSourceProxy(ds);
        Jdbi jdbi = Jdbi.create(proxy);
        jdbi.setSqlLogger(new Slf4JSqlLogger());
        jdbiPlugins.forEach(plugin -> jdbi.installPlugin(plugin));
        return jdbi;

    }

    @Bean
    List<JdbiPlugin> jdbiPlugins() {
        return Arrays.asList(new PostgresPlugin());
    }
}
