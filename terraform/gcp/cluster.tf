
resource "google_container_cluster" "juniper_cluster" {
  name = "juniper-cluster"

  location                 = var.region
  enable_autopilot         = true

  # Set `deletion_protection` to `true` will ensure that one cannot
  # accidentally delete this instance by use of Terraform.
  deletion_protection = false

  depends_on = [
    google_project_service.enable_gke,
    google_project_service.enable_iam,
    google_project_service.enable_dns,
    google_project_service.enable_secret_manager,
    google_project_service.enable_artifact_registry,
    google_project_service.enable_sql,
    google_project_service.enable_sql_admin
  ]
}

data "google_compute_default_service_account" "default" {
}

# assigns at folder level so that the service account can read from the separate artifact registry project
resource "google_folder_iam_binding" "cluster-artifact-registry-reader" {
  project = var.project
  role    = "roles/artifactregistry.reader"
  folder = "folders/${var.juniper_folder_id}"
  members = [
    "serviceAccount:${data.google_compute_default_service_account.default.email}"
  ]
}

resource "google_project_iam_binding" "cluster-log-write" {
  project = var.project
  role    = "roles/logging.logWriter"
  members = [
    "serviceAccount:${data.google_compute_default_service_account.default.email}"
  ]
}


data "google_container_cluster" "juniper_cluster" {
  name     = "juniper-cluster"
  location = var.region
}
