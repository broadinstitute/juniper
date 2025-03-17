# Creates the service account for the GKE autopilot cluster.

resource "google_service_account" "cluster_service_account" {
  account_id = "juniper-cluster"
}

resource "google_project_iam_binding" "cluster-metric-writer" {
  project = var.project
  role    = "roles/monitoring.metricWriter"
  members = [
    "serviceAccount:${google_service_account.cluster_service_account.email}"
  ]
}

resource "google_project_iam_binding" "cluster-monitoring-viewer" {
  project = var.project
  role    = "roles/monitoring.viewer"
  members = [
    "serviceAccount:${google_service_account.cluster_service_account.email}"
  ]
}

resource "google_project_iam_binding" "cluster-log-writer" {
  project = var.project
  role    = "roles/logging.logWriter"
  members = [
    "serviceAccount:${google_service_account.cluster_service_account.email}",
    "serviceAccount:${google_service_account.juniper_cloudbuild_service_account.email}"
  ]
}

# As far as I can tell, the GKE service account that manages the cluster cannot be changed, but
# it needs to be able to access the KMS key to encrypt and decrypt the database.
resource "google_kms_key_ring_iam_binding" "cluster-key-ring" {
  key_ring_id = google_kms_key_ring.juniper_cluster_keyring.id
  members = [
    "serviceAccount:service-${var.project_number}@container-engine-robot.iam.gserviceaccount.com"
  ]
  role        = "roles/cloudkms.cryptoKeyEncrypterDecrypter"
}

