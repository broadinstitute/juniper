package bio.terra.pearl.core.dao.search;

import bio.terra.pearl.core.model.address.MailingAddress;
import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.Family;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.model.search.EnrolleeSearchExpressionResult;
import bio.terra.pearl.core.model.survey.Answer;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.service.search.EnrolleeSearchExpression;
import bio.terra.pearl.core.service.search.sql.EnrolleeSearchQueryBuilder;
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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

@Component
public class EnrolleeSearchExpressionDao {
    private final Jdbi jdbi;

    public EnrolleeSearchExpressionDao(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    public List<EnrolleeSearchExpressionResult> executeSearch(EnrolleeSearchExpression expression, UUID studyEnvId) {
        return executeSearch(expression.generateQueryBuilder(studyEnvId));
    }

    private List<EnrolleeSearchExpressionResult> executeSearch(EnrolleeSearchQueryBuilder search) {
        return jdbi.withHandle(handle -> {
            org.jooq.Query jooqQuery = search.toQuery(DSL.using(SQLDialect.POSTGRES));
            Query query = jdbiFromJooq(jooqQuery, handle);
            return query
                    .registerRowMapper(Family.class, BeanMapper.of(Family.class, "family"))
                    .registerRowMapper(EnrolleeSearchExpressionResult.class, new EnrolleeSearchResultMapper())
                    .reduceRows(new EnrolleeSearchResultReducer())
                    .toList();
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
            EnrolleeSearchExpressionResult enrolleeSearchExpressionResult = new EnrolleeSearchExpressionResult();

            enrolleeSearchExpressionResult.setEnrollee(
                    BeanMapper.of(Enrollee.class, "enrollee")
                            .map(rs, ctx)
            );

            enrolleeSearchExpressionResult.setProfile(
                    BeanMapper.of(Profile.class, "profile")
                            .map(rs, ctx)
            );

            // anything that starts with "task" will be added to the tasks list
            mapAllBeans(
                    rs,
                    ctx,
                    ParticipantTask.class,
                    "task",
                    enrolleeSearchExpressionResult.getTasks()::add
            );

            mapAllBeans(
                    rs,
                    ctx,
                    Answer.class,
                    "answer",
                    enrolleeSearchExpressionResult.getAnswers()::add
            );

            mapBean(rs,
                    ctx,
                    MailingAddress.class,
                    "mailing_address",
                    enrolleeSearchExpressionResult::setMailingAddress);

            mapBean(rs,
                    ctx,
                    KitRequest.class,
                    "latest_kit",
                    enrolleeSearchExpressionResult::setLatestKit);

            return enrolleeSearchExpressionResult;
        }

        private boolean isColumnPresent(ResultSet rs, String columnName) {
            try {
                rs.findColumn(columnName);
                return true;
            } catch (SQLException e) {
                return false;
            }
        }


        private <T> void mapBean(
                ResultSet rs,
                StatementContext ctx,
                Class<T> clazz,
                String prefix,
                Consumer<T> callback) throws SQLException {
            if (isColumnPresent(rs, prefix + "_id")) {
                callback.accept(BeanMapper.of(clazz, prefix).map(rs, ctx));
            }
        }

        private <T> void mapAllBeans(
                ResultSet rs,
                StatementContext ctx,
                Class<T> clazz,
                String prefix,
                Consumer<T> callback) throws SQLException {
            // Loop through all the columns to see if any of the possible extra objects
            // are present.
            // (the column count starts from 1)
            for (int i = 1; i < rs.getMetaData().getColumnCount(); i++) {
                String columnName = rs.getMetaData().getColumnName(i);
                if (columnName.startsWith(prefix) && columnName.endsWith("_created_at")) {
                    String modelName = columnName.substring(
                            0,
                            columnName.length() - "_created_at".length());
                    callback.accept(BeanMapper.of(clazz, modelName).map(rs, ctx));
                }
            }
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
}
