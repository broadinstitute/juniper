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
    "serviceAccount:${google_service_account.cluster_service_account.email}"
  ]
}

resource "google_artifact_registry_repository_iam_binding" "cluster-artifact-registry-reader" {
  role   = "roles/artifactregistry.reader"
  repository = "juniper"
  members = [
    "serviceAccount:${google_service_account.cluster_service_account.email}"
  ]

  # create it in the infra project not the current project
  project = var.infra_project
  location = var.infra_region
  provider = google.infra
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

