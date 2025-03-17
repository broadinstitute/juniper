
resource "google_service_account" "malware_scanner_sa" {
  account_id = "juniper-malware-scanner"
  project    = var.project
  display_name = "juniper-malware-scanner"
}

resource "google_project_iam_member" "malware_scanner_iam" {
  # need: log writer and metric writer
  for_each = toset(["roles/run.invoker", "roles/eventarc.eventReceiver", "roles/monitoring.metricWriter", "roles/logging.logWriter"])
  project  = var.project
  role     = each.value
  member   = "serviceAccount:${google_service_account.malware_scanner_sa.email}"
}

resource "google_service_account" "build_service_account" {
  account_id   = "juniper-malware-scanner-build"
  display_name = "Service Account for malware scanner cloud run service"
}

resource "google_project_iam_binding" "build_iam" {
  for_each = toset(["roles/storage.objectViewer", "roles/artifactregistry.writer"])
  project  = var.project
  role     = each.value
  members  = ["serviceAccount:${google_service_account.build_service_account.email}"]
}

## Allow GCS to publish to pubsub
#
data "google_storage_project_service_account" "juniper_gcs_accounts" {
  for_each = var.juniper_projects
  project = each.value
}

data "google_storage_project_service_account" "infra-gs" {
    project = var.project
}

resource "google_project_iam_binding" "gcs_sa_pubsub_publish" {

  project = var.project
  role    = "roles/pubsub.publisher"
  members = concat([
    for sa in data.google_storage_project_service_account.juniper_gcs_accounts: "serviceAccount:${sa.email_address}"
  ], [
    "serviceAccount:${google_service_account.malware_scanner_sa.email}",
    "serviceAccount:${data.google_storage_project_service_account.infra-gs.email_address}"
  ])
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
