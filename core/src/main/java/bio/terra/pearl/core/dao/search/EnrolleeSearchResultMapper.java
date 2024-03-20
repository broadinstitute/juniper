package bio.terra.pearl.core.dao.search;

import bio.terra.pearl.core.model.address.MailingAddress;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.model.search.EnrolleeSearchResult;
import bio.terra.pearl.core.model.survey.Answer;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

public class EnrolleeSearchResultMapper implements RowMapper<EnrolleeSearchResult> {
    @Override
    public EnrolleeSearchResult map(ResultSet rs, StatementContext ctx) throws SQLException {
        EnrolleeSearchResult enrolleeSearchResult = new EnrolleeSearchResult();

        enrolleeSearchResult.setEnrollee(
                BeanMapper.of(Enrollee.class, "enrollee")
                        .map(rs, ctx)
        );

        enrolleeSearchResult.setProfile(
                BeanMapper.of(Profile.class, "profile")
                        .map(rs, ctx)
        );


        // Loop through all the columns to see if any of the possible extra objects
        // are present. We cannot check on their existence without throwing and catching
        // SQL exceptions, so it is better to loop through columns to check presence.
        // (the column count starts from 1)
        for (int i = 1; i < rs.getMetaData().getColumnCount(); i++) {
            String columnName = rs.getMetaData().getColumnName(i);

            if (columnName.startsWith("answer_") && columnName.endsWith("_created_at")) {
                String questionStableId = columnName.substring("answer_".length(),
                        columnName.length() - "_created_at".length());

                enrolleeSearchResult.getAnswers().add(
                        BeanMapper.of(Answer.class, "answer_" + questionStableId)
                                .map(rs, ctx)
                );
            }

            if (columnName.startsWith("mailing_address_")) {
                enrolleeSearchResult.setMailingAddress(
                        BeanMapper.of(MailingAddress.class, "mailing_address")
                                .map(rs, ctx)
                );
            }
        }

        return enrolleeSearchResult;
    }

}
