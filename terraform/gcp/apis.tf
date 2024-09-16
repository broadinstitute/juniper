resource "google_project_service" "enable_iam" {
  project = var.project
  service = "iam.googleapis.com"
}

resource "google_project_service" "enable_gke" {
  project = var.project
  service = "container.googleapis.com"
}


resource "google_project_service" "enable_dns" {
  project = var.project
  service = "dns.googleapis.com"
}

resource "google_project_service" "enable_secret_manager" {
  project = var.project
  service = "secretmanager.googleapis.com"
}

resource "google_project_service" "enable_artifact_registry" {
  project = var.project
  service = "artifactregistry.googleapis.com"
}

resource "google_project_service" "enable_sql" {
  project = var.project
  service = "sql-component.googleapis.com"
}

resource "google_project_service" "enable_sql_admin" {
  project = var.project
  service = "sqladmin.googleapis.com"
}
