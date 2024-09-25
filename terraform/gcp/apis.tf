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

resource "google_project_service" "enable_sql" {
  project = var.project
  service = "sql-component.googleapis.com"
}

resource "google_project_service" "enable_sql_admin" {
  project = var.project
  service = "sqladmin.googleapis.com"
}

resource "google_project_service" "enable_cloud_logging" {
  project = var.project
  service = "logging.googleapis.com"
}

resource "google_project_service" "enable_recommender" {
  project = var.project
  service = "recommender.googleapis.com"
}

resource "google_project_service" "enable_service_networking" {
  service = "servicenetworking.googleapis.com"
  project = var.project
}
