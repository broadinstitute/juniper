## Data export
Data export is the process of exporting all useful participant information to flat files suitable for
analysis, by matching data to corresponding stableIds.

### Process
Exporting is a four-stage process, structured so that each step can be optimized for performance separately
The biggest complexities of the process are that the number and names of the columns to be exported cannot
be fully determined until after all participant data has been read.  This is because if a participant has
completed a given survey more than once, or given multiple answers to a list question (e.g. name all your doctors),
we want to put each answer in its own column.  And therefore we won't know the final column list until the 
last participant data is read.  Accordingly, the process is:

1. Read options from the customer, along with study environment configuration information, to generate
a list of "ModuleExportInfo".  A "module" corresponds to a releated chunk of data to export.  Examples of modules
are "Profile" or a single Survey.  The ModuleExportInfo has all the metadata later stages of the export proces will
need to produce the export
2. Load the participant data from the database.  This is currently do relatievly naively, one participant at a time.  Later,
we'll want to upgrade to more sophisticated batching techniques.
3. Use the ModuleExportInfos to parse each enrollee's data into a String->String hashmap for that enrollee, where each
entry roughly corresponds to a single data point.
4. Pass the generated hashmaps for every enrollee to an exporter, which writes them out as tsv, json, or .xlsx as appropriate

