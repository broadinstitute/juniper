### Extraction Services

Extraction is the inverse of populate.  While populate takes a pointer to a file, and then populates the 
database with the configuration from the files, extraction takes portal configuration from the database
and writes it to a zip file.  The folder structure and the file contents that result from the extraction
should be as close as possible to the original files that were used to populate the database.

Most importantly, the files produced by extraction should be easily human readable and editable.  this will facilitate using
these files as a starting point for test cases.

Roughly speaking, the extraction process is:
1. go to the populate UX and go to "extract"
2. type the shortcode of a portal (e.g. 'ourhealth')
3. observe a zip file is downloaded.
4. The zip file can then be inspected or modified, or uploaded via the populate UI
