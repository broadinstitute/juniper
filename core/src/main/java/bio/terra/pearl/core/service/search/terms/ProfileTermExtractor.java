package bio.terra.pearl.core.service.search.terms;

import bio.terra.pearl.core.service.search.EnrolleeSearchContext;
import bio.terra.pearl.core.service.search.sql.SQLJoinClause;
import bio.terra.pearl.core.service.search.sql.SQLSelectClause;
import bio.terra.pearl.core.service.search.sql.SQLWhereClause;
import bio.terra.pearl.core.service.search.sql.SQLWhereField;
import lombok.Getter;

import java.util.List;

public class ProfileTermExtractor implements EnrolleeTermExtractor{

    private final ProfileField field;

    public ProfileTermExtractor(ProfileField field) {
        this.field = field;
    }

    @Override
    public Term extract(EnrolleeSearchContext context) {
        switch (field) {
            case GIVEN_NAME -> {
                return new Term(context.getProfile().getGivenName());
            }
            case FAMILY_NAME -> {
                return new Term(context.getProfile().getFamilyName());
            }
            case CONTACT_EMAIL -> {
                return new Term(context.getProfile().getContactEmail());
            }
            case PHONE_NUMBER -> {
                return new Term(context.getProfile().getPhoneNumber());
            }
            case BIRTH_DATE -> {
                return new Term(context.getProfile().getBirthDate());
            }
            default -> {
                return null;
            }
        }
    }

    @Override
    public List<SQLJoinClause> requiredJoinClauses() {
        return List.of(new SQLJoinClause("profile", "profile", "enrollee.profile_id = profile.id"));
    }

    @Override
    public List<SQLSelectClause> requiredSelectClauses() {
        return List.of(new SQLSelectClause("profile", field.getValue()));
    }

    @Override
    public SQLWhereClause termClause() {
        return new SQLWhereField("profile", field.getValue());
    }

    @Getter
    public enum ProfileField {
        GIVEN_NAME("given_name"),
        FAMILY_NAME("family_name"),
        CONTACT_EMAIL("contact_email"),
        PHONE_NUMBER("phone_number"),
        BIRTH_DATE("birth_date");
        final String value;

        ProfileField(String value) {
            this.value = value;
        }

    }

}
