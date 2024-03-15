# Search Expressions

This package contains code which allows creating enrollee search expressions.

Search expressions work by creating a nested tree of `EnrolleeSearchExpression` objects. These objects can be used
to either match against a provided enrollee or search all enrollees in a study environment with SQL.

You can create a search expression either by creating the objects by hand or by using the 
`EnrolleeSearchExpressionParser`. 

Example of creating a search expression by hand:

```java
EnrolleeSearchExpression expression = new BooleanSearchExpression(
        new EnrolleeSearchFacet(
                new ProfileTermExtractor(ProfileTermExtractor.ProfileField.GIVEN_NAME),
                new ConstantTermExtractor(new Term("Jonas")),
                ComparisonOperator.EQUALS),
        new EnrolleeSearchFacet(
                new ProfileTermExtractor(ProfileTermExtractor.ProfileField.FAMILY_NAME),
                new ConstantTermExtractor(new Term("Salk")),
                ComparisonOperator.EQUALS),
        BooleanOperator.AND);
```

Example of creating an equivalent search expression using the parser:
```java
enrolleeSearchExpressionParser.parse("{profile.givenName} = 'Jonas' AND {profile.familyName} = 'Salk'");
```

Search expressions have a variety of components. They have expressions (`BooleanSearchExpression`), facets 
(`EnrolleeSearchFacet`), and term extractors (`ProfileTermExtractor`, `ConstantTermExtractor`). `ConstantTermExtractor` 
is special as it will always return the same value, while other term extractors will use the provided information
(e.g., field name) to extract the value from the enrollee.

You can invoke expressions to match against an enrollee or to generate SQL.

```java
expression.evaluate(new EnrolleeSearchContext(enrollee, profile));

# or 

# Creates a search object which will search for enrollees in a specific
# study environment.
SQLSearch search = expression.generateSqlSearch(studyEnvId);
enrolleeSearchExpressionDao.search(search);
```