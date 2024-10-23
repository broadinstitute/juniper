package bio.terra.pearl.core.service.search.terms;

import lombok.Getter;
import org.apache.commons.beanutils.NestedNullException;
import org.apache.commons.beanutils.PropertyUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Result of a search term (profile.givenName) or user input ("John"). Useful when executing a
 * search expression outside of SQL.
 */
@Getter
public class SearchValue {
    private String stringValue = null;
    private Double numberValue = null;
    private Instant instantValue = null;
    private LocalDate dateValue = null;
    private Boolean booleanValue = null;
    private List<SearchValue> arrayValue = null;

    private final SearchValueType searchValueType;

    public SearchValue(String stringValue) {
        this.stringValue = stringValue;
        this.searchValueType = SearchValueType.STRING;
    }

    public SearchValue(Double numberValue) {
        this.numberValue = numberValue;
        this.searchValueType = SearchValueType.NUMBER;
    }

    public SearchValue(Instant instantValue) {
        this.instantValue = instantValue;
        this.searchValueType = SearchValueType.INSTANT;
    }

    public SearchValue(LocalDate dateValue) {
        this.dateValue = dateValue;
        this.searchValueType = SearchValueType.DATE;
    }

    public SearchValue(Boolean booleanValue) {
        this.booleanValue = booleanValue;
        this.searchValueType = SearchValueType.BOOLEAN;
    }

    public SearchValue(List<SearchValue> arrayValue) {
        this.arrayValue = arrayValue;
        this.searchValueType = SearchValueType.ARRAY;
    }

    public SearchValue() {
        this.searchValueType = SearchValueType.NULL;
    }

    public static SearchValue of(Object objValue, SearchValueType type) {
        try {
            return switch (type) {
                case STRING -> new SearchValue(objValue.toString());
                case DATE -> new SearchValue((LocalDate) objValue);
                case INSTANT -> new SearchValue((Instant) objValue);
                case NUMBER -> new SearchValue((Double) objValue);
                case BOOLEAN -> new SearchValue((Boolean) objValue);
                default -> throw new IllegalArgumentException("Invalid field type: " + type);
            };
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Invalid field type: " + type);
        }
    }

    public static SearchValue ofNestedProperty(Object object, String field, SearchValueType type) {
        try {
            Object objValue = PropertyUtils.getNestedProperty(object, field);
            return of(objValue, type);
        } catch (NestedNullException | NullPointerException e) {
            return new SearchValue();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid field: " + field);
        }
    }

    private Object getValue() {
        return switch (this.searchValueType) {
            case STRING -> this.stringValue;
            case NUMBER -> this.numberValue;
            case INSTANT -> this.instantValue;
            case DATE -> this.dateValue;
            case BOOLEAN -> this.booleanValue;
            case ARRAY -> this.arrayValue;
            default -> null;
        };
    }

    public boolean equals(SearchValue right) {
        if (this.isArray() || right.isArray()) {
            return arrayEquals(right);
        }

        Object leftValue = this.getValue();
        Object rightValue = right.getValue();

        if (leftValue == null || rightValue == null) {
            return leftValue == null && rightValue == null;
        }

        if (this.searchValueType != right.searchValueType) {
            return false;
        }

        return leftValue.equals(rightValue);
    }

    private boolean isArray() {
        return this.searchValueType.equals(SearchValueType.ARRAY);
    }

    private boolean arrayEquals(SearchValue right) {
        if (!this.isArray() && !right.isArray()) {
            return false;
        }

        // if both are arrays, compare the arrays
        if (this.isArray() && right.isArray()) {
            if (this.arrayValue.size() != right.arrayValue.size()) {
                return false;
            }

            for (int i = 0; i < this.arrayValue.size(); i++) {
                if (!this.arrayValue.get(i).equals(right.arrayValue.get(i))) {
                    return false;
                }
            }

            return true;
        }

        // if one is an array and the other is not, compare
        // like SQL by checking if the single value is in the array

        // either left or right value could be the array, so check both
        SearchValue singleValue = !this.isArray() ? this : right;
        List<SearchValue> array = this.isArray() ? this.arrayValue : right.arrayValue;

        if (singleValue.getValue() == null || array == null) {
            return false;
        }

        return array.stream().anyMatch(innerVal -> innerVal.equals(singleValue));
    }

    public boolean greaterThan(SearchValue right) {
        if (this.searchValueType != right.searchValueType) {
            return false;
        }

        if (this.getValue() == null || right.getValue() == null) {
            return false;
        }

        return switch (this.searchValueType) {
            case NUMBER -> this.numberValue > right.numberValue;
            case INSTANT -> this.instantValue.isAfter(right.instantValue);
            default -> false;
        };
    }

    public boolean greaterThanOrEqualTo(SearchValue right) {
        if (this.searchValueType != right.searchValueType) {
            return false;
        }

        if (this.getValue() == null || right.getValue() == null) {
            return false;
        }

        return switch (this.searchValueType) {
            case NUMBER -> this.numberValue >= right.numberValue;
            case INSTANT -> this.instantValue.isAfter(right.instantValue) || this.instantValue.equals(right.instantValue);
            default -> false;
        };
    }

    public boolean contains(SearchValue rightSearchValue) {
        if (this.searchValueType.equals(SearchValueType.ARRAY) || rightSearchValue.searchValueType.equals(SearchValueType.ARRAY)) {
            return this.equals(rightSearchValue);
        }

        if (this.searchValueType != SearchValueType.STRING || rightSearchValue.searchValueType != SearchValueType.STRING) {
            return false;
        }

        return this.stringValue.toLowerCase().contains(rightSearchValue.stringValue.toLowerCase());
    }

    public enum SearchValueType {
        STRING,
        NUMBER,
        INSTANT,
        DATE,
        BOOLEAN,
        ARRAY,
        NULL
    }
}
