package bio.terra.pearl.core.dao.search;

import bio.terra.pearl.core.dao.participant.EnrolleeDao;
import bio.terra.pearl.core.dao.participant.ProfileDao;
import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.model.address.MailingAddress;
import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.participant.*;
import bio.terra.pearl.core.model.search.EnrolleeSearchExpressionResult;
import bio.terra.pearl.core.model.survey.Answer;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.service.search.EnrolleeSearchExpression;
import bio.terra.pearl.core.service.search.EnrolleeSearchOptions;
import bio.terra.pearl.core.service.search.expressions.DefaultSearchExpression;
import bio.terra.pearl.core.service.search.sql.EnrolleeSearchQueryBuilder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;
import org.jdbi.v3.core.result.LinkedHashMapRowReducer;
import org.jdbi.v3.core.result.RowView;
import org.jdbi.v3.core.statement.Query;
import org.jdbi.v3.core.statement.StatementContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

@Component
@Slf4j
public class EnrolleeSearchExpressionDao {
    private final Jdbi jdbi;
    private final EnrolleeDao enrolleeDao;
    private final ProfileDao profileDao;

    /** list of mappers for the various modules that can be included in a search result */
    protected static final List<SearchModuleMapper<? extends BaseEntity>> moduleMappers = List.of(
            new SearchModuleMapper<>("enrollee", Enrollee.class, EnrolleeSearchExpressionResult::setEnrollee),
            new SearchModuleMapper<>("profile", Profile.class, EnrolleeSearchExpressionResult::setProfile),
            new SearchModuleMapper<>("portalUser", PortalParticipantUser.class, EnrolleeSearchExpressionResult::setPortalParticipantUser),
            new SearchModuleMapper<>("participant_user", ParticipantUser.class, EnrolleeSearchExpressionResult::setParticipantUser),
            new SearchModuleMapper<>("mailing_address", MailingAddress.class, EnrolleeSearchExpressionResult::setMailingAddress),
            new SearchModuleMapper<>("latest_kit", KitRequest.class, EnrolleeSearchExpressionResult::setLatestKit),
            new SearchModuleCollectionMapper<>("answer", Answer.class, (result, answer) -> result.getAnswers().add(answer)),
            new SearchModuleCollectionMapper<>("task", ParticipantTask.class, (result, task) -> result.getTasks().add(task)));


    public EnrolleeSearchExpressionDao(Jdbi jdbi, EnrolleeDao enrolleeDao, ProfileDao profileDao) {
        this.jdbi = jdbi;
        this.enrolleeDao = enrolleeDao;
        this.profileDao = profileDao;
    }

    public List<EnrolleeSearchExpressionResult> executeSearch(EnrolleeSearchExpression expression, UUID studyEnvId) {
        if (expression == null) {
            expression = new DefaultSearchExpression(enrolleeDao, profileDao);
        }
        return executeSearch(expression.generateQueryBuilder(studyEnvId), EnrolleeSearchOptions.builder().build());
    }

    public List<EnrolleeSearchExpressionResult> executeSearch(EnrolleeSearchExpression expression, UUID studyEnvId, EnrolleeSearchOptions opts) {
        if (expression == null) {
            expression = new DefaultSearchExpression(enrolleeDao, profileDao);
        }
        return executeSearch(expression.generateQueryBuilder(studyEnvId), opts);
    }

    public List<EnrolleeSearchExpressionResult> executeSearch(EnrolleeSearchQueryBuilder search, EnrolleeSearchOptions opts) {
        return jdbi.withHandle(handle -> {
            org.jooq.Query jooqQuery = search.toQuery(DSL.using(SQLDialect.POSTGRES), opts);
            Query query = jdbiFromJooq(jooqQuery, handle);
            var result = query
                    .registerRowMapper(Family.class, BeanMapper.of(Family.class, "family"))
                    .registerRowMapper(EnrolleeSearchExpressionResult.class, new EnrolleeSearchResultMapper())
                    .reduceRows(new EnrolleeSearchResultReducer())
                    .toList();
            return result;
        });
    }

    private static Query jdbiFromJooq(org.jooq.Query jooqQuery, Handle handle) {
        Query query = handle.createQuery(jooqQuery.getSQL());
        for (int i = 0; i < jooqQuery.getBindValues().size(); i++) {
            query.bind(i, jooqQuery.getBindValues().get(i));
        }
        return query;
    }

    /**
     * Maps a row from the database to an {@link EnrolleeSearchExpressionResult}. Each row contains
     * prefixed columns for all the related search objects (e.g. enrollee, profile, answers, etc.),
     * and some objects are conditionally included only if the user used them in their query.
     */
    public static class EnrolleeSearchResultMapper implements RowMapper<EnrolleeSearchExpressionResult> {
        @Override
        public EnrolleeSearchExpressionResult map(ResultSet rs, StatementContext ctx) throws SQLException {
            EnrolleeSearchExpressionResult result = new EnrolleeSearchExpressionResult();
            for (SearchModuleMapper processor : moduleMappers) {
                processor.map(rs, ctx, result);
            }
            return result;
        }

    }

    /**
     * Reduces rows from the database into a single {@link EnrolleeSearchExpressionResult} object.
     */
    public static class EnrolleeSearchResultReducer implements LinkedHashMapRowReducer<UUID, EnrolleeSearchExpressionResult> {
        @Override
        public void accumulate(Map<UUID, EnrolleeSearchExpressionResult> map, RowView rowView) {
            final EnrolleeSearchExpressionResult searchResult = map.computeIfAbsent(rowView.getColumn("enrollee_id", UUID.class),
                    id -> rowView.getRow(EnrolleeSearchExpressionResult.class));

            // Add family to enrollee
            if (isColumnPresent(rowView, "family_id", UUID.class)) {
                searchResult.getFamilies().add(rowView.getRow(Family.class));
            }
        }

        private <T> boolean isColumnPresent(RowView rv, String columnName, Class<T> c) {
            try {
                return rv.getColumn(columnName, c) != null;
            } catch (Exception e) {
                return false;
            }
        }
    }

    /** for simple beans that need to be mapped to a property in the EnrolleeSearchExpressionResult, like Profile */
    @Getter
    private static class SearchModuleMapper<T> {
        protected final String prefix;
        protected final RowMapper<T> mapper;
        protected final BiConsumer<EnrolleeSearchExpressionResult,T> consumer;
        protected final Class<T> clazz;

        public SearchModuleMapper(String prefix, Class<T> clazz, BiConsumer<EnrolleeSearchExpressionResult, T> consumer) {
            this.prefix = prefix;
            this.clazz = clazz;
            this.mapper = BeanMapper.of(clazz, prefix);
            this.consumer = consumer;
        }

        public void map(ResultSet rs, StatementContext ctx, EnrolleeSearchExpressionResult result) throws SQLException {
            if (isColumnPresent(rs, prefix + "_id")) {
               consumer.accept(result, mapper.map(rs, ctx));
            }
        }

        private boolean isColumnPresent(ResultSet rs, String columnName) {
            try {
                rs.findColumn(columnName);
                return true;
            } catch (SQLException e) {
                return false;
            }
        }
    }

    /** for modules that are mapped to a collection in the EnrolleeSearchExpressionResult, such as tasks */
    private static class SearchModuleCollectionMapper<T> extends SearchModuleMapper<T> {
        public SearchModuleCollectionMapper(String prefix, Class<T> clazz, BiConsumer<EnrolleeSearchExpressionResult, T> consumer) {
            super(prefix, clazz, consumer);
        }

        @Override
        public void map(ResultSet rs, StatementContext ctx, EnrolleeSearchExpressionResult result) throws SQLException {
            for (int i = 1; i < rs.getMetaData().getColumnCount(); i++) {
                String columnName = rs.getMetaData().getColumnName(i);
                if (columnName.startsWith(prefix) && columnName.endsWith("_created_at")) {
                    String itemPrefix = columnName.substring(
                            0,
                            columnName.length() - "_created_at".length());
                    RowMapper<T> itemMapper = BeanMapper.of(clazz, itemPrefix);
                    consumer.accept(result, itemMapper.map(rs, ctx));
                }
            }
        }
    }
}
