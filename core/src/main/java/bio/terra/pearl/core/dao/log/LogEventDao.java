package bio.terra.pearl.core.dao.log;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.model.log.LogEvent;
import java.util.List;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;
import org.springframework.stereotype.Component;

/** Note that this doesn't extend BaseJdbiDao since LogEvent does not extend BaseEntity (no lastUpdated field) */
@Component
public class LogEventDao {
  private Jdbi jdbi;

  public LogEventDao(Jdbi jdbi) {
    this.jdbi = jdbi;
    jdbi.registerRowMapper(LogEvent.class, BeanMapper.of(LogEvent.class));
  }

  private static final List<String> insertFields = List.of("createdAt", "eventType", "eventName", "eventSource",
      "portalShortcode", "studyShortcode", "environmentName", "enrolleeShortcode", "operatorId", "eventDetail", "stackTrace");
  private static final String CREATE_QUERY_SQL = "insert into log_event (" +
      insertFields.stream().map(field -> BaseJdbiDao.toSnakeCase(field)).collect(Collectors.joining(", ")) +
      " ) values ( " + insertFields.stream().map(field -> ":" + field).collect(Collectors.joining(", ")) + ");";


  public LogEvent create(LogEvent event) {
    return jdbi.withHandle(handle ->
        handle.createUpdate(CREATE_QUERY_SQL)
            .bindBean(event)
            .executeAndReturnGeneratedKeys()
            .mapTo(LogEvent.class)
            .one()
    );
  }

}
