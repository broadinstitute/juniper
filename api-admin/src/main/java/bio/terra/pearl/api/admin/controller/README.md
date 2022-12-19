Together with the code generated from openapi.yml, a Controller is responsible for:

* implementing `*Api` methods
* calling `*Service` methods
* translating Service results (including exceptions) into HTTP responses

Controllers do not directly access DAOs and should delegate the behavior of the system to Service methods.