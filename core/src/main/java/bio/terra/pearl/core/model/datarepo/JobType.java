package bio.terra.pearl.core.model.datarepo;

/* TODO: Rename to ExportStep, and rename table to DATA_REPO_EXPORT_STEP?

    There are several steps needed in order for the TDR export to succeed.
    First, a dataset must be created. This only needs to happen once per study environment.
    Second, we upload the CSV to an azure storage container. We will generate a SAS-signed URL which will be fed into the next step.
    Third, we wi



 */

public enum JobType {
    CREATE_DATASET,
    INGEST_DATASET,
    UPLOAD_CSV
}
