package bio.terra.pearl.core.service.search.expressions;

import lombok.Getter;

@Getter
public enum UnarySearchOperator {
    IS_NOT_NULL("IS NOT NULL"),
    IS_NULL("IS NULL");

    private final String operator;

    UnarySearchOperator(String operator) {
        this.operator = operator;
    }

}
