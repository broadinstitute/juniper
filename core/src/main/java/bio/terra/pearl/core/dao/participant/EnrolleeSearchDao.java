package bio.terra.pearl.core.dao.participant;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.dao.study.StudyEnvironmentDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.kit.KitRequestStatus;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.EnrolleeSearchResult;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.participant.search.facets.sql.SqlSearchableFacet;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;
import org.jdbi.v3.core.statement.Query;
import org.springframework.stereotype.Component;

@Component
public class EnrolleeSearchDao {
  private final Jdbi jdbi;
  private StudyEnvironmentDao studyEnvironmentDao;
  private EnrolleeDao enrolleeDao;
  private ProfileDao profileDao;
  private String baseSelectString;
  private RowMapper<Enrollee> enrolleeRowMapper;
  private RowMapper<Profile> profileRowMapper;
  private RowMapper<ParticipantUser> participantUserRowMapper;

  public EnrolleeSearchDao(StudyEnvironmentDao studyEnvironmentDao, Jdbi jdbi,
                           EnrolleeDao enrolleeDao, ProfileDao profileDao,
                           ParticipantUserDao participantUserDao) {
    this.studyEnvironmentDao = studyEnvironmentDao;
    this.jdbi = jdbi;
    this.enrolleeDao = enrolleeDao;
    this.profileDao = profileDao;
    baseSelectString = "select distinct on (enrollee.id) " +
        generateSelectString(enrolleeDao) + ", " + generateSelectString(profileDao) +
            ", participant_user.last_login, participant_user.username " +
        ", kit_request.status as kit_request__status";
    enrolleeRowMapper = BeanMapper.of(Enrollee.class, enrolleeDao.getTableName() + "__");
    profileRowMapper = BeanMapper.of(Profile.class, profileDao.getTableName() + "__");
    participantUserRowMapper = BeanMapper.of(ParticipantUser.class, participantUserDao.getTableName() + "__");
  }

  public List<EnrolleeSearchResult> search(String studyShortcode, EnvironmentName envName,
                                             List<SqlSearchableFacet> facets) {
    StudyEnvironment studyEnv = studyEnvironmentDao.findByStudy(studyShortcode, envName).get();
    return search(studyEnv.getId(), facets);
  }

  protected List<EnrolleeSearchResult> search(UUID studyEnvId, List<SqlSearchableFacet> facets) {
    List<EnrolleeSearchResult> result = jdbi.withHandle(handle -> {
      String queryString = generateSearchQueryString(facets);
      Query query = handle.createQuery(queryString);
      query.bind("studyEnvironmentId", studyEnvId);

      for (int i = 0; i < facets.size(); i++) {
        facets.get(i).bindSqlParameters(i, query);
      }
      return query
          .registerRowMapper(Enrollee.class, enrolleeRowMapper)
          .registerRowMapper(Profile.class, profileRowMapper)
          .reduceRows(new LinkedHashMap<UUID, EnrolleeSearchResult>(),
              // see https://jdbi.org/#_resultbearing_reducerows
              // we don't technically need to use reduce rows yet since we just return one row per enrollee
              // but I suspect that will change as we start adding more complex joins.
              (map, rowView) -> {
                EnrolleeSearchResult esr = map.computeIfAbsent(rowView.getColumn("enrollee__id", UUID.class),
                    id -> new EnrolleeSearchResult());
                esr.setEnrollee(rowView.getRow(Enrollee.class));
                esr.setProfile(rowView.getRow(Profile.class));
                esr.setParticipantUser(rowView.getRow(ParticipantUser.class));
                esr.setMostRecentKitStatus(rowView.getColumn("kit_request__status", KitRequestStatus.class));
                return map;
              })
          .values()
          .stream().toList();
    });
    return result;
  }

  protected String generateSearchQueryString(List<SqlSearchableFacet> facets) {
    HashMap<String, List<SqlSearchableFacet>> facetsGroupByTable = new HashMap<String, List<SqlSearchableFacet>>();
    facets.stream()
        // filter out enrollee and profile -- those will already be included
        .filter(facet -> !List.of("enrollee", "profile").contains(facet.getTableName()))
        .forEach(facet -> {
          if (!facetsGroupByTable.containsKey(facet.getTableName())) {
            facetsGroupByTable.put(facet.getTableName(), new ArrayList<>());
          }
          facetsGroupByTable.get(facet.getTableName()).add(facet);
        });

    List<String> selects = facets.stream().map(facet -> facet.getSelectQuery())
        .filter(query -> query != null)
        .collect(Collectors.toList());
    selects.add(0, baseSelectString);
    String selectQuery = selects.stream().collect(Collectors.joining(","));

    String baseFromQuery = """
             from enrollee 
             left join participant_user on participant_user.id = enrollee.participant_user_id
             left join profile on profile.id = enrollee.profile_id 
             left join kit_request on enrollee.id = kit_request.enrollee_id
    """;
    List<String> froms = facetsGroupByTable.values().stream().map(facetList -> facetList.get(0).getJoinQuery())
        .collect(Collectors.toList());
    froms.add(0, baseFromQuery);
    String fromQuery = froms.stream().collect(Collectors.joining(""));

    String baseWhereQuery = " where enrollee.study_environment_id = :studyEnvironmentId";
    List<String> wheres = IntStream.range(0, facets.size()).mapToObj(i ->
        facets.get(i).getWhereClause(i)).collect(Collectors.toList());
    wheres.add(0, baseWhereQuery);
    String whereQuery = wheres.stream().collect(Collectors.joining(" AND"));

    String sortQuery = " order by enrollee.id, enrollee.created_at desc, kit_request.created_at desc;";

    String sqlQuery = selectQuery + fromQuery + whereQuery + sortQuery;
    return sqlQuery;
  }

  protected static String generateSelectString(BaseJdbiDao dao) {
    List<String> getCols = dao.getGetQueryColumns();
    String tableName = dao.getTableName();
    String fieldString = getCols.stream().map(colName -> "%s.%s AS %s__%s".formatted(tableName, colName, tableName, colName))
        .collect(Collectors.joining(", "));
    return fieldString;
  }
}
