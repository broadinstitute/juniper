
resource "google_compute_network" "juniper_network" {
  name = "juniper-cluster-network"

  auto_create_subnetworks  = false
  enable_ula_internal_ipv6 = true
}

resource "google_compute_subnetwork" "juniper_subnetwork" {
  name = "juniper-cluster-subnetwork"

  ip_cidr_range = "10.0.0.0/16"
  region        = var.region

  stack_type       = "IPV4_IPV6"
  ipv6_access_type = "EXTERNAL" # Change to "EXTERNAL" if creating an external loadbalancer

  network = google_compute_network.juniper_network.id
  secondary_ip_range {
    range_name    = "services-range"
    ip_cidr_range = "192.168.0.0/18"
  }

  secondary_ip_range {
    range_name    = "pod-ranges"
    ip_cidr_range = "192.168.64.0/18"
  }
}

resource "google_container_cluster" "juniper_cluster" {
  name = "juniper-cluster"

  location                 = var.region
  enable_autopilot         = true
  enable_l4_ilb_subsetting = true

  network    = google_compute_network.juniper_network.id
  subnetwork = google_compute_subnetwork.juniper_subnetwork.id

  ip_allocation_policy {
    stack_type                    = "IPV4_IPV6"
    services_secondary_range_name = google_compute_subnetwork.juniper_subnetwork.secondary_ip_range[0].range_name
    cluster_secondary_range_name  = google_compute_subnetwork.juniper_subnetwork.secondary_ip_range[1].range_name
  }

  # Set `deletion_protection` to `true` will ensure that one cannot
  # accidentally delete this instance by use of Terraform.
  deletion_protection = false

  depends_on = [
    google_project_service.enable_gke,
    google_project_service.enable_dns,
    google_project_service.enable_secret_manager,
    google_project_service.enable_cloud_logging,
    google_project_service.enable_sql,
    google_project_service.enable_sql_admin
  ]
}

data "google_compute_default_service_account" "default" {
}

resource "google_artifact_registry_repository_iam_binding" "cluster-artifact-registry-reader" {
  role   = "roles/artifactregistry.reader"
  repository = "juniper"
  members = [
    "serviceAccount:${data.google_compute_default_service_account.default.email}"
  ]

  # create it in the infra project not the current project
  project = var.infra_project
  location = var.infra_region
  provider = google.infra
}

resource "google_project_iam_binding" "cluster-log-write" {
  project = var.project
  role    = "roles/logging.logWriter"
  members = [
    "serviceAccount:${data.google_compute_default_service_account.default.email}"
  ]
}
