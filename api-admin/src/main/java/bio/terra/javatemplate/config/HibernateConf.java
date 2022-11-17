package bio.terra.javatemplate.config;

import java.util.Properties;
import javax.sql.DataSource;
import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class HibernateConf {

  @Bean
  public LocalSessionFactoryBean sessionFactory() {
    LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
    sessionFactory.setDataSource(dataSource);
    String[] packagesToScan = {env.getProperty("hibernate.packages-to-scan")};
    sessionFactory.setPackagesToScan(packagesToScan);
    sessionFactory.setHibernateProperties(hibernateProperties());
    sessionFactory.setPhysicalNamingStrategy(new CamelCaseToUnderscoresNamingStrategy());

    return sessionFactory;
  }

  private final Properties hibernateProperties() {
    Properties hibernateProperties = new Properties();
    hibernateProperties.setProperty(
        "hibernate.hbm2ddl.auto", env.getProperty("spring.jpa.hibernate.ddl-auto"));

    hibernateProperties.setProperty(
        "hibernate.dialect", env.getProperty("spring.jpa.database-platform"));

    hibernateProperties.setProperty(
        "hibernate.show_sql", env.getProperty("spring.jpa.show-sql", "false"));
    hibernateProperties.setProperty(
        "hibernate.format_sql",
        env.getProperty("spring.jpa.properties.hibernate.format_sql", "false"));
    return hibernateProperties;
  }

  @Autowired private DataSource dataSource;

  @Autowired private Environment env;
}
