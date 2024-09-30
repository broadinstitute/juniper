resource "google_compute_network" "juniper_network" {
  name = "juniper-cluster-network"

  auto_create_subnetworks  = false
  enable_ula_internal_ipv6 = true

  depends_on = [
    time_sleep.enable_all_services_with_timeout
  ]
}

resource "google_compute_subnetwork" "juniper_subnetwork" {
  name = "juniper-cluster-subnetwork"

  ip_cidr_range = "10.0.0.0/16"
  region        = var.region

  stack_type       = "IPV4_IPV6"
  ipv6_access_type = "INTERNAL"

  private_ip_google_access = true

  network = google_compute_network.juniper_network.id

  log_config {
    aggregation_interval = "INTERVAL_10_MIN"
    flow_sampling        = 0.5
    metadata             = "INCLUDE_ALL_METADATA"
  }
}

# google service peering needed for cloud sql access
resource "google_compute_global_address" "private_ip_address" {
  provider = google-beta

  project       = var.project
  name          = "private-ip-address"
  purpose       = "VPC_PEERING"
  address_type  = "INTERNAL"
  prefix_length = 16
  network       = google_compute_network.juniper_network.id
}

resource "google_service_networking_connection" "private_vpc_connection" {
  provider = google-beta

  network                 = google_compute_network.juniper_network.id
  service                 = "servicenetworking.googleapis.com"
  reserved_peering_ranges = [google_compute_global_address.private_ip_address.name]
}


# creating a router and cloud nat is required for juniper to have internet access
resource "google_compute_router" "juniper-router" {
  project = var.project
  name    = "juniper-router"
  network = google_compute_network.juniper_network.id
  region  = var.region
}

module "cloud-nat" {
  source                             = "terraform-google-modules/cloud-nat/google"
  version                            = "~> 5.0"
  project_id                         = var.project
  region                             = var.region
  router                             = google_compute_router.juniper-router.name
  name                               = "juniper-nat-config"
  source_subnetwork_ip_ranges_to_nat = "ALL_SUBNETWORKS_ALL_IP_RANGES"
}
