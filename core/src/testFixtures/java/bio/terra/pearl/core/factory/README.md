### Factories

Factories are provided to automate away as much of default object-creation as possible so that tests 
can focus just on the behavior under tests.

Our factories provide two methods:
1. `builder(String testName)` Provides a builder for the object with properties initialized to reasonable
values.  This ensures, e.g. that required simple properties (but *not* required relationships) are present.
Where possible, these values will be randomized and/or incorporate the `testName` parameter to 
help guarantee test independence and traceability.


2. `builderWithDependencies(String testName)` Similar to buider(), but this also takes care of 
creating and saving any needed upstream relationships for an object.  For example, an
enrollee will need a studyEnvironment.  Calling this method ensures that the returned object
can be saved simply by calling `.build()` and then passing the object to the appropriate DAO or
service
