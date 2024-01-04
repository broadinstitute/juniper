### Extraction Services

Extraction is the inverse of populate.  While populate takes a pointer to a file, and then populates the 
database with the configuration from the files, extraction takes portal configuration from the database
and writes it to a zip file.  The folder structure and the file contents that result from the extraction
should be as close as possible to the original files that were used to populate the database.

Most importantly, the files produced by extraction should be easily human readable and editable.  this will facilitate using
these files as a starting point for test cases.
