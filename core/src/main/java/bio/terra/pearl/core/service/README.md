### Services

Guidelines for services:

1. A service should generally only access one DAO.  Querying and updating entities not owned by the 
service should be done via other services.  Note this means some services may depend on each other
   (although that should be avoided where practical)  in this case see https://www.baeldung.com/circular-dependencies-in-spring
for techniques to allow the mutual dependency
