locals {
  enable_services = [
    "container.googleapis.com",
    "containersecurity.googleapis.com",
    "dns.googleapis.com",
    "secretmanager.googleapis.com",
    "sql-component.googleapis.com",
    "sqladmin.googleapis.com",
    "logging.googleapis.com",
    "recommender.googleapis.com",
    "servicenetworking.googleapis.com",
    "cloudkms.googleapis.com",
    "binaryauthorization.googleapis.com",
  ]
}

resource "google_project_service" "enable_all_services" {
  for_each = toset(local.enable_services)

  project = var.project
  service = each.value
  disable_on_destroy = false
}

resource "time_sleep" "enable_all_services_with_timeout" {
  create_duration = "3m"

  depends_on = [
    google_project_service.enable_all_services
  ]
}
