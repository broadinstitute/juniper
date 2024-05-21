package bio.terra.pearl.core.service.search.expressions;

import lombok.Getter;

@Getter
public enum SearchOperators {
    EQUALS("="),
    NOT_EQUALS("!="),
    GREATER_THAN(">"),
    LESS_THAN("<"),
    GREATER_THAN_EQ(">="),
    LESS_THAN_EQ("<="),
    CONTAINS("contains");

    private final String operator;

    SearchOperators(String operator) {
        this.operator = operator;
    }

}
