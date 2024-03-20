package bio.terra.pearl.core.dao.search;

import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.model.search.EnrolleeSearchResult;
import bio.terra.pearl.core.model.survey.Answer;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;

public class EnrolleeSearchResultMapper implements RowMapper<EnrolleeSearchResult> {
    @Override
    public EnrolleeSearchResult map(ResultSet rs, StatementContext ctx) throws SQLException {
        EnrolleeSearchResult enrolleeSearchResult = new EnrolleeSearchResult();

        enrolleeSearchResult.setEnrollee(
                Enrollee.builder()
                        .id(rs.getObject("enrollee_id", UUID.class))
                        .profileId(rs.getObject("enrollee_profile_id", UUID.class))
                        .createdAt(rs.getObject("enrollee_created_at", Timestamp.class).toInstant())
                        .lastUpdatedAt(rs.getObject("enrollee_last_updated_at", Timestamp.class).toInstant())
                        .participantUserId(rs.getObject("enrollee_participant_user_id", UUID.class))
                        .studyEnvironmentId(rs.getObject("enrollee_study_environment_id", UUID.class))
                        .build()
        );

        enrolleeSearchResult.setProfile(
                Profile.builder()
                        .id(rs.getObject("profile_id", UUID.class))
                        .givenName(rs.getString("profile_given_name"))
                        .familyName(rs.getString("profile_family_name"))
                        .createdAt(rs.getObject("profile_created_at", Timestamp.class).toInstant())
                        .lastUpdatedAt(rs.getObject("profile_last_updated_at", Timestamp.class).toInstant())
                        .build()
        );

        // The column count starts from 1
        for (int i = 1; i < rs.getMetaData().getColumnCount(); i++) {
            String columnName = rs.getMetaData().getColumnName(i);

            if (columnName.startsWith("answer_") && columnName.endsWith("_created_at")) {

                String questionStableId = columnName.substring("answer_".length(),
                        columnName.length() - "_created_at".length());

                enrolleeSearchResult.getAnswers().add(
                        Answer.builder()
                                .id(rs.getObject("answer_" + questionStableId + "_id", UUID.class))
                                .questionStableId("answer_" + questionStableId + "_question_stable_id")
                                .stringValue(rs.getString("answer_" + questionStableId + "_string_value"))
                                .booleanValue(rs.getBoolean("answer_" + questionStableId + "_boolean_value"))
                                .numberValue(rs.getDouble("answer_" + questionStableId + "_number_value"))
                                .objectValue(rs.getString("answer_" + questionStableId + "_object_value"))
//                                .answerType(AnswerType.valueOf(rs.getString("answer_" + questionStableId + "_answer_type")))
                                .surveyResponseId(rs.getObject("answer_" + questionStableId + "_survey_response_id", UUID.class))
                                .enrolleeId(rs.getObject("answer_" + questionStableId + "_enrollee_id", UUID.class))
                                .surveyStableId(rs.getString("answer_" + questionStableId + "_survey_stable_id"))
                                .otherDescription(rs.getString("answer_" + questionStableId + "_other_description"))
                                .surveyVersion(rs.getInt("answer_" + questionStableId + "_survey_version"))
                                .viewedLanguage(rs.getString("answer_" + questionStableId + "_viewed_language"))
                                .creatingAdminUserId(rs.getObject("answer_" + questionStableId + "_creating_admin_user_id", UUID.class))
                                .creatingParticipantUserId(rs.getObject("answer_" + questionStableId + "_creating_participant_user_id", UUID.class))
                                .createdAt(rs.getObject("answer_" + questionStableId + "_created_at", Timestamp.class).toInstant())
                                .lastUpdatedAt(rs.getObject("answer_" + questionStableId + "_last_updated_at", Timestamp.class).toInstant())
                                .build()
                );

            }
        }

        return enrolleeSearchResult;
    }

}
