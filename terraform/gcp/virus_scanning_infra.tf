
resource "google_service_account" "malware_scanner_sa" {
  account_id = "juniper-malware-scanner"
  project    = var.project
  display_name = "juniper-malware-scanner"
}



resource "google_project_iam_member" "malware_scanner_iam" {
  for_each = toset(["roles/run.invoker", "roles/eventarc.eventReceiver"])
  project  = var.project
  role     = each.value
  member   = "serviceAccount:${google_service_account.malware_scanner_sa.email}"
}

resource "google_service_account" "build_service_account" {
  account_id   = "juniper-malware-scanner-build"
  display_name = "Service Account for malware scanner cloud run service"
}

resource "google_project_iam_binding" "malware-scanner-object-viewer" {
  project  = var.project
  role     = "roles/storage.objectViewer"
  members  = ["serviceAccount:${google_service_account.build_service_account.email}"]
}

## Allow GCS to publish to pubsub
#
data "google_storage_project_service_account" "gcs_account" {
}

resource "google_project_iam_binding" "gcs_sa_pubsub_publish" {
  project = var.project
  role    = "roles/pubsub.publisher"
  members = ["serviceAccount:${data.google_storage_project_service_account.gcs_account.email_address}"]
}

## Allow service account to admin the scanner buckets.
#
# They may not have been created by TF, so use a data resource
# to verify their existence.
#


resource "google_storage_bucket_iam_binding" "unscanned_buckets_sa_binding" {
  bucket   = google_storage_bucket.unscanned_participant_documents.name
  role     = "roles/storage.admin"
  members = [
    "serviceAccount:${google_service_account.malware_scanner_sa.email}",
  ]
}

resource "google_storage_bucket_iam_binding" "clean_buckets_sa_binding" {
  bucket   = google_storage_bucket.clean_participant_documents.name
  role     = "roles/storage.admin"
  members = [
    "serviceAccount:${google_service_account.malware_scanner_sa.email}",
  ]
}

resource "google_storage_bucket_iam_binding" "quarantined_buckets_sa_binding" {
  bucket   = google_storage_bucket.quarantined_participant_documents.name
  role     = "roles/storage.admin"
  members = [
    "serviceAccount:${google_service_account.malware_scanner_sa.email}",
  ]
}

resource "google_storage_bucket_iam_binding" "cvd_buckets_sa_binding" {
  bucket   = google_storage_bucket.cvd_mirror_bucket.name
  role     = "roles/storage.admin"
  members = [
    "serviceAccount:${google_service_account.malware_scanner_sa.email}",
  ]
}

## Create the CVD Mirror bucket and allow service account admin access.
#

resource "google_storage_bucket_iam_binding" "cvd_mirror_bucket_sa_binding" {
  bucket = google_storage_bucket.cvd_mirror_bucket.name
  role   = "roles/storage.admin"
  members = [
    "serviceAccount:${google_service_account.malware_scanner_sa.email}",
  ]
}
