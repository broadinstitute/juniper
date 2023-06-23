package bio.terra.pearl.core.dao.participant;

import bio.terra.pearl.core.dao.study.StudyEnvironmentDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.EnrolleeSearchResult;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.participant.search.facets.sql.SqlSearchableFacet;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Timestamp;
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
  private ObjectMapper objectMapper;

  public EnrolleeSearchDao(StudyEnvironmentDao studyEnvironmentDao, Jdbi jdbi,
                           EnrolleeDao enrolleeDao, ObjectMapper objectMapper) {
    this.studyEnvironmentDao = studyEnvironmentDao;
    this.jdbi = jdbi;
    this.enrolleeDao = enrolleeDao;
    this.objectMapper = objectMapper;
  }

  public List<EnrolleeSearchResult> search(String studyShortcode, EnvironmentName envName,
                                             List<SqlSearchableFacet> facets) {
    StudyEnvironment studyEnv = studyEnvironmentDao.findByStudy(studyShortcode, envName).get();
    return search(studyEnv.getId(), facets);
  }


  protected List<EnrolleeSearchResult> search(UUID studyEnvId, List<SqlSearchableFacet> facets) {
    var result =  jdbi.withHandle(handle -> {
      String queryString = generateSearchQueryString(facets);
      Query query = handle.createQuery(queryString);
      query.bind("studyEnvironmentId", studyEnvId);

      for (int i = 0; i < facets.size(); i++) {
        facets.get(i).bindSqlParameters(i, query);
      }
      return query.mapToMap().list();
    });
    return result.stream().map(row -> transform(row)).toList();
  }

  protected String generateSearchQueryString(List<SqlSearchableFacet> facets) {
    var facetsGroupByTable = new HashMap<String, List<SqlSearchableFacet>>();
    facets.stream()
        // filter out enrollee and profile -- those will already be included
        .filter(facet -> !List.of("enrollee", "profile").contains(facet.getTableName()))
        .forEach(facet -> {
          if (!facetsGroupByTable.containsKey(facet.getTableName())) {
            facetsGroupByTable.put(facet.getTableName(), new ArrayList<>());
          }
          facetsGroupByTable.get(facet.getTableName()).add(facet);
        });

    /** these select fields are very hardcoded.  A better iteration will use the getSelectFields from the DAOs */
    String baseSelectQuery = """
      select distinct on (enrollee.id)
       enrollee.shortcode as enrollee__shortcode,
       enrollee.created_at as enrollee__created_at,
       enrollee.last_updated_at as enrollee__last_updated_at,
       enrollee.consented as enrollee__consented,
       profile.sex_at_birth as profile__sex_at_birth,
       profile.given_name as profile__given_name,
       profile.family_name as profile__family_name
     """;
    List<String> selects = facets.stream().map(facet -> facet.getSelectQuery())
        .filter(query -> query != null)
        .collect(Collectors.toList());
    selects.add(0, baseSelectQuery);
    String selectQuery = selects.stream().collect(Collectors.joining(","));

    String baseFromQuery = " from enrollee left join profile on profile.id = enrollee.profile_id";
    List<String> froms = facetsGroupByTable.values().stream().map(facetList -> facetList.get(0).getJoinQuery())
        .collect(Collectors.toList());
    froms.add(0, baseFromQuery);
    String fromQuery = froms.stream().collect(Collectors.joining(""));

    String baseWhereQuery = " where enrollee.study_environment_id = :studyEnvironmentId";
    List<String> wheres = IntStream.range(0, facets.size()).mapToObj(i ->
        facets.get(i).getWhereClause(i)).collect(Collectors.toList());
    wheres.add(0, baseWhereQuery);
    String whereQuery = wheres.stream().collect(Collectors.joining(" AND"));

    String sortQuery = " order by enrollee.id, enrollee.created_at desc;";

    String sqlQuery = selectQuery + fromQuery + whereQuery + sortQuery;
    return sqlQuery;
  }

  /** this method is currently very hardcoded as a POC.  A better iteration would use the jdbi row mappers from the
   * respective DAOs.
   * This also should do a better job of nulling out fields that are not fetched to avoid confusion (or just fetching them)
   * */
  protected EnrolleeSearchResult transform(Map<String, Object> daoResult) {
    return EnrolleeSearchResult.builder()
        .enrollee(Enrollee.builder()
            .shortcode((String) daoResult.get("enrollee__shortcode"))
            .createdAt(((Timestamp) daoResult.get("enrollee__created_at")).toInstant())
            .lastUpdatedAt(((Timestamp) daoResult.get("enrollee__last_updated_at")).toInstant())
            .consented((Boolean) daoResult.get("enrollee__consented")).build())
        .profile(Profile.builder()
            .sexAtBirth((String) daoResult.get("profile__sex_at_birth"))
            .givenName((String) daoResult.get("profile__given_name"))
            .familyName((String) daoResult.get("profile__family_name")).build()
        ).build();
  }
}
