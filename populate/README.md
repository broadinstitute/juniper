## Populate module

Populate code is concerned with persisting portal configurations to the database from flat files.  This enables us
to spin up portals in a reqpeatable way for demo and development purposes.  Populate utilities also handle populating synthetic
test participants.  The goal of development population, is that any user journey should be very easily 
accessible and reproducible by populating a given portal that has configuration and synthetic participants to
support seeing any given step in the journey without having to manually create the configuration and participants.

When developing a new feature, especially if that feature involves a new type of configuration, ensure that the populate infrastructure 
supports populating and testing that configuration.  Both a populate (persisting the configuration to the database) and an extraction
(taking the configuration from the database and writing it to a zip file) should be supported.
