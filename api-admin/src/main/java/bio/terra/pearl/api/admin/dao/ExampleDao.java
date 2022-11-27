package bio.terra.pearl.api.admin.dao;

import bio.terra.pearl.api.admin.model.Example;
import io.opencensus.contrib.spring.aop.Traced;
import java.util.Optional;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ExampleDao {
  private static final RowMapper<Example> EXAMPLE_ROW_MAPPER =
      (rs, rowNum) ->
          new Example(rs.getLong("id"), rs.getString("user_id"), rs.getString("message"));

  private final NamedParameterJdbcTemplate jdbcTemplate;

  public ExampleDao(NamedParameterJdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Traced
  public void upsertExample(Example example) {
    var query =
        "INSERT INTO example (user_id, message)"
            + " VALUES (:userId, :message)"
            + " ON CONFLICT (user_id) DO UPDATE SET"
            + " message = excluded.message";

    var namedParameters =
        new MapSqlParameterSource()
            .addValue("userId", example.userId())
            .addValue("message", example.message());

    jdbcTemplate.update(query, namedParameters);
  }

  @Traced
  public Optional<Example> getExampleForUser(String userId) {
    var namedParameters = new MapSqlParameterSource().addValue("userId", userId);
    var selectSql = "SELECT * FROM example WHERE user_id = :userId";
    return Optional.ofNullable(
        DataAccessUtils.singleResult(
            jdbcTemplate.query(selectSql, namedParameters, EXAMPLE_ROW_MAPPER)));
  }
}
