### Services

Guidelines for services:

1. A service should generally only access one DAO.  Querying and updating entities not owned by the 
service should generally be done via other services.  Note this means some services may depend on each other
   (although that should be avoided where practical)  in this case see https://www.baeldung.com/circular-dependencies-in-spring
for techniques to allow the mutual dependency
2. Create, Update and Destroy are the most important methods to adhere to the above pattern. The owning service for
an entity should be the single source of business logic for required logic for creation/deletion.
For querying, especially performance-optimized loading queries across multiple entities, direct dao
access from non-owning services is less problematic.

3. For CRUD services
   1. *create* the create method should return the object created, along with any children created attached.
   The default is to create any not-already existing children that are attached to the object
   passed to the create method
   2. *delete*  the delete method should automatically 
   delete any dependent/owned entities.  the delete method should take a 'cascades' argument
   that allows setting of non-dependent entities that should be deleted if orphaned.  this cascades method 
   should be passed along to other service-delete methods called.  An enu 'AllowedCascades' can be
   provided in the service to specify which non-dependent deletes are implemented.
