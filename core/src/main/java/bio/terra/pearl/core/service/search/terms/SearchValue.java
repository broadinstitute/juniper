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

    public boolean equals(SearchValue right) {
        if (!this.searchValueType.equals(SearchValueType.ARRAY) && right.searchValueType.equals(SearchValueType.ARRAY)) {
            // flip the comparison if the right side is an array so that we hit the array comparison logic
            return right.equals(this);
        }

        if (this.searchValueType != right.searchValueType && !this.searchValueType.equals(SearchValueType.ARRAY)) {
            return false;
        }

        return switch (this.searchValueType) {
            case STRING -> this.stringValue.equals(right.stringValue);
            case NUMBER -> this.numberValue.equals(right.numberValue);
            case INSTANT -> this.instantValue.equals(right.instantValue);
            case BOOLEAN -> this.booleanValue.equals(right.booleanValue);
            case DATE -> this.dateValue.equals(right.dateValue);
            case ARRAY -> {
                if (right.getSearchValueType().equals(SearchValueType.ARRAY)) {
                    yield this.arrayValue.equals(right.arrayValue);
                }
                // like SQL, we allow comparing a single value to an array
                // by checking if the single value is in the array
                yield this.arrayValue.stream().anyMatch(innerVal -> innerVal.equals(right));
            }
            default -> false;
        };
    }

    public boolean greaterThan(SearchValue right) {
        if (this.searchValueType != right.searchValueType) {
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
