The DAO layer interacts with the database. DAO concrete classes derive from BaseJdbiDao, via BaseMutableJdbiDao or
BaseVersionedJdbiDao, and each is associated with an entity class that acts as a data transfer object (DTO).
Those entity classes are may include nested entity classes, which are also DTOs. Typically, the nested entity classes
are not instantiated by the associated DAO class, so callers should assume those nested entity classes are empty unless 
the DAO method explicitly documents otherwise. 

Nested entity classes are instantiated at various points in the code, often for the purpose of populating a response
object. A common pattern for a service class method that takes an entity class argument with nested entities is to 
take the nested entities as separate arguments, rather than relying on the entity class to have the nested entities 
attached.

When creating a DAO test:
1. the DAO test should test its own DAO, but not call any other DAOs.
To the extent it needs to create/read other objects, it should do so via factories and services.
2. the DAO test should be @Transactional so that any DB changes are rolled back
