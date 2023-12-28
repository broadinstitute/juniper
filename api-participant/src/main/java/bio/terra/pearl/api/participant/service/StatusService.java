package bio.terra.pearl.api.participant.service;

import bio.terra.pearl.api.participant.config.StatusCheckConfiguration;
import bio.terra.pearl.api.participant.model.SystemStatusSystems;
import java.sql.Connection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class StatusService extends BaseStatusService {
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
      log.debug("Checking database connection valid");
      return new SystemStatusSystems()
          .ok(jdbcTemplate.getJdbcTemplate().execute((Connection conn) -> conn.isValid(5000)));
    } catch (Exception ex) {
      String errorMsg = "Database status check failed";
      log.error(errorMsg, ex);
      return new SystemStatusSystems().ok(false).addMessagesItem(errorMsg + ": " + ex.getMessage());
    }
  }
}
