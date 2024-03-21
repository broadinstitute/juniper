# Search Expressions

The search package allows searching through enrollee records to create a cohort.
Search expressions are specified in the form of a string rule, e.g.,
`{age} > 18 and {answer.demographics_survey.sex_at_birth} = 'F'`. These can then be
parsed into a search expression object, which allows either the creation of a SQL
query or direct evaluation on an enrollee.

```java
EnrolleeSearchExpression exp = enrolleeSearchExpressionParser.parse("{age} > 18");

boolean match = exp.evaluate(new EnrolleeSearchContext(enrollee, profile));
// or
List<EnrolleeSearchResult> results = enrolleeSearchExpressionDao.executeSearch(exp, studyEnvId);
```

When running a SQL search, all related objects will be joined and selected in their entirety
and added to the search result. For example, the full answer object will be included in the
results for every answer referenced in the query.