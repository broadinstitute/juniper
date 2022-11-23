When creating a DAO test:
1. the DAO test should test its own DAO, but not call any other DAOs.
To the extent it needs to create/read other objects, it should do so via factories and services.
2. the DAO test should be @Transactional so that any DB changes are rolled back
