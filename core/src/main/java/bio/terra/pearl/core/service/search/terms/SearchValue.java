package bio.terra.pearl.core.service.search.terms;

import lombok.Getter;
import org.apache.commons.beanutils.NestedNullException;
import org.apache.commons.beanutils.PropertyUtils;

import java.time.Instant;
import java.time.LocalDate;

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
        return switch (this.searchValueType) {
            case STRING -> this.stringValue.equals(right.stringValue);
            case NUMBER -> {
                yield this.numberValue.equals(right.numberValue);
            }
            case INSTANT -> this.instantValue.equals(right.instantValue);
            case BOOLEAN -> this.booleanValue.equals(right.booleanValue);
            default -> false;
        };
    }

    public boolean greaterThan(SearchValue right) {
        return switch (this.searchValueType) {
            case NUMBER -> {
                yield this.numberValue > right.numberValue;
            }
            case INSTANT -> this.instantValue.isAfter(right.instantValue);
            default -> false;
        };
    }

    public boolean greaterThanOrEqualTo(SearchValue right) {
        return switch (this.searchValueType) {
            case NUMBER -> {
                yield this.numberValue >= right.numberValue;
            }
            case INSTANT -> this.instantValue.isAfter(right.instantValue) || this.instantValue.equals(right.instantValue);
            default -> false;
        };
    }

    public boolean contains(SearchValue rightSearchValue) {
        if (this.searchValueType != SearchValueType.STRING || rightSearchValue.searchValueType != SearchValueType.STRING) {
            return false;
        }

        return this.stringValue.contains(rightSearchValue.stringValue);
    }

    public enum SearchValueType {
        STRING,
        NUMBER,
        INSTANT,
        DATE,
        BOOLEAN,
        NULL
    }
}
