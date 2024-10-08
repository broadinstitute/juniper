resource "google_project_service" "enable_bucket_storage" {
  service = "storage.googleapis.com"
  project = var.project
}

resource "google_project_service" "enable_artifact_registry" {
  service = "artifactregistry.googleapis.com"
  project = var.project
}

resource "google_project_service" "enable_iam_creds" {
  service = "iamcredentials.googleapis.com"
  project = var.project
}
