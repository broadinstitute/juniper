package bio.terra.pearl.core.dao.participant;

import bio.terra.pearl.core.dao.study.StudyEnvironmentDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.participant.search.facets.sql.SqlSearchableFacet;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.Query;
import org.springframework.stereotype.Component;

@Component
public class EnrolleeSearchDao {
  private final Jdbi jdbi;
  private StudyEnvironmentDao studyEnvironmentDao;
  private EnrolleeDao enrolleeDao;

  public EnrolleeSearchDao(StudyEnvironmentDao studyEnvironmentDao, Jdbi jdbi, EnrolleeDao enrolleeDao) {
    this.studyEnvironmentDao = studyEnvironmentDao;
    this.jdbi = jdbi;
    this.enrolleeDao = enrolleeDao;
  }

  public List<Map<String, Object>> search(String studyShortcode, EnvironmentName envName,
                                          List<SqlSearchableFacet> facets) {
    StudyEnvironment studyEnv = studyEnvironmentDao.findByStudy(studyShortcode, envName).get();
    return search(studyEnv.getId(), facets);
  }

  public List<Map<String, Object>> search(UUID studyEnvId, List<SqlSearchableFacet> facets) {
    var result =  jdbi.withHandle(handle -> {
      String queryString = generateSearchQueryString(facets);
      Query query = handle.createQuery(queryString);
      query.bind("studyEnvironmentId", studyEnvId);

      for (int i = 0; i < facets.size(); i++) {
        facets.get(i).bindSqlParameters(i, query);
      }
      return query.mapToMap().list();
    });
    return result;
  }

  protected String generateSearchQueryString(List<SqlSearchableFacet> facets) {
    var facetsGroupByTable = new HashMap<String, List<SqlSearchableFacet>>();
    facets.stream()
        .filter(facet -> !"enrollee".equals(facet.getTableName()))
        .forEach(facet -> {
          if (!facetsGroupByTable.containsKey(facet.getTableName())) {
            facetsGroupByTable.put(facet.getTableName(), new ArrayList<>());
          }
          facetsGroupByTable.get(facet.getTableName()).add(facet);
        });

    String baseSelectQuery = """
      select enrollee.id, max(enrollee.shortcode) as enrollee__shortcode, 
       max(enrollee.created_at) as enrollee__created_at,
       bool_and(enrollee.consented) as enrollee__consented
     """;
    List<String> selects = facets.stream().map(facet -> facet.getSelectQuery())
        .collect(Collectors.toList());
    selects.add(0, baseSelectQuery);
    String selectQuery = selects.stream().collect(Collectors.joining(","));

    String baseFromQuery = " from enrollee";
    List<String> froms = facetsGroupByTable.values().stream().map(facetList -> facetList.get(0).getJoinQuery())
        .collect(Collectors.toList());
    froms.add(0, baseFromQuery);
    String fromQuery = froms.stream().collect(Collectors.joining(""));

    String baseWhereQuery = " where enrollee.study_environment_id = :studyEnvironmentId";
    List<String> wheres = IntStream.range(0, facets.size()).mapToObj(i ->
        facets.get(i).getWhereClause(i)).collect(Collectors.toList());
    wheres.add(0, baseWhereQuery);
    String whereQuery = wheres.stream().collect(Collectors.joining(" AND"));

    String groupByQuery = " group by enrollee.id";
    String sortQuery = " order by enrollee.created_at desc;";

    String sqlQuery = selectQuery + fromQuery + whereQuery + groupByQuery + sortQuery;
    return sqlQuery;
  }
}
