package bio.terra.javatemplate.service;

import bio.terra.javatemplate.config.StatusCheckConfiguration;
import bio.terra.javatemplate.model.SystemStatusSystems;
import java.sql.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class StatusService extends BaseStatusService {
  private static final Logger logger = LoggerFactory.getLogger(StatusService.class);

  private final NamedParameterJdbcTemplate jdbcTemplate;

  @Autowired
  public StatusService(
      NamedParameterJdbcTemplate jdbcTemplate, StatusCheckConfiguration configuration) {
    super(configuration);
    this.jdbcTemplate = jdbcTemplate;
    registerStatusCheck("CloudSQL", this::databaseStatus);
  }

  private SystemStatusSystems databaseStatus() {
    try {
      logger.debug("Checking database connection valid");
      return new SystemStatusSystems()
          .ok(jdbcTemplate.getJdbcTemplate().execute((Connection conn) -> conn.isValid(5000)));
    } catch (Exception ex) {
      String errorMsg = "Database status check failed";
      logger.error(errorMsg, ex);
      return new SystemStatusSystems().ok(false).addMessagesItem(errorMsg + ": " + ex.getMessage());
    }
  }
}
