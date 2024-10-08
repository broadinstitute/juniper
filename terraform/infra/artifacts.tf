
resource "google_artifact_registry_repository" "juniper_repo" {
  location      = var.region
  repository_id = "juniper"
  description   = "juniper repository"
  format        = "DOCKER"

  depends_on = [
    google_project_service.enable_artifact_registry
  ]
}
