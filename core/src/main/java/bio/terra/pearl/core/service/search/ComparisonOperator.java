package bio.terra.pearl.core.service.search;

import lombok.Getter;

@Getter
public enum ComparisonOperator {
    EQUALS("="),
    NOT_EQUALS("!="),
    GREATER_THAN(">"),
    LESS_THAN("<"),
    GREATER_THAN_EQ(">="),
    LESS_THAN_EQ("<=");

    private final String operator;

    ComparisonOperator(String operator) {
        this.operator = operator;
    }

    public String getOperator() {
        return operator;
    }

}
