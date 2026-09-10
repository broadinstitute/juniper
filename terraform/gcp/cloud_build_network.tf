# In order for cloud build to access our private GKE instance,
# we create a separate network with a VM instance acting as a NAT gateway.
# This VM instance has a public IP and the GKE cluster allows
# this public IP to access the master node. All traffic from cloud build
# runs through the NAT gateway and thus has access to GKE.
# See: https://cloud.google.com/build/docs/private-pools/accessing-private-gke-clusters-with-cloud-build-private-pools

resource "google_compute_network" "cloud_build_network" {
  name = "juniper-cloud-build-network"

  auto_create_subnetworks  = false

  depends_on = [
    time_sleep.enable_all_services_with_timeout
  ]
}

resource "google_compute_subnetwork" "cloud_build_subnetwork" {
  name = "juniper-cloud-build-subnetwork"

  ip_cidr_range = "10.1.0.0/24"
  region        = var.region

  stack_type       = "IPV4_ONLY"

  private_ip_google_access = true

  network = google_compute_network.cloud_build_network.id

  dynamic "log_config" {
    for_each = var.enable_flow_logs ? [1] : []
    content {
      aggregation_interval = "INTERVAL_10_MIN"
      flow_sampling        = 0.5
      metadata             = "INCLUDE_ALL_METADATA"
    }
  }
}


# google service peering needed for cloud sql access
resource "google_compute_global_address" "cloud_build_peering_address" {
  provider = google-beta

  project       = var.project
  name          = "cloud-build-peering-address"
  purpose       = "VPC_PEERING"
  address_type  = "INTERNAL"
  address       = "10.2.0.0"
  prefix_length = 24
  network       = google_compute_network.cloud_build_network.id
}

resource "google_service_networking_connection" "cloud_build_peering_connection" {
  provider = google-beta

  network                 = google_compute_network.cloud_build_network.id
  service                 = "servicenetworking.googleapis.com"
  reserved_peering_ranges = [google_compute_global_address.cloud_build_peering_address.name]
}


resource "google_compute_network_peering_routes_config" "service_networking_peering_config" {
  project = var.project
  peering = google_service_networking_connection.cloud_build_peering_connection.peering
  network = google_compute_network.cloud_build_network.name

  export_custom_routes = true
  import_custom_routes = true

  depends_on = [
    google_service_networking_connection.cloud_build_peering_connection
  ]
}

resource "google_service_account" "cloudbuild_nat_user" {
  account_id   = "cloudbuild-nat-user"
  display_name = "Custom SA for VM Instance"
}

resource "google_compute_address" "public_cloud_build_ip" {
  name = "cloud-build-ip"
  region = var.region

}

resource "google_compute_instance" "cloud_build_nat_vm" {
  name         = "cloud-build-nat-vm"
  machine_type = var.cloud_build_nat_machine_type
  zone         = "${var.region}-a"

  tags = ["direct-gateway-access", "nat-gateway"]

  can_ip_forward = true

  # resizing the VM requires it to be stopped first; the startup script
  # re-establishes the iptables masquerade rule on boot
  allow_stopping_for_update = true


  boot_disk {
    initialize_params {
      image = "debian-cloud/debian-11"
    }
  }
  network_interface {
    network = google_compute_network.cloud_build_network.id
    subnetwork = google_compute_subnetwork.cloud_build_subnetwork.id

    access_config {
      nat_ip = google_compute_address.public_cloud_build_ip.address
    }
  }

  metadata_startup_script = <<EOT
sysctl -w net.ipv4.ip_forward=1
iptables -t nat -A POSTROUTING -o $(ip addr show scope global | head -1 | awk -F: '{print $2}') -j MASQUERADE
  EOT

  service_account {
    email  = google_service_account.cloudbuild_nat_user.email
    scopes = ["cloud-platform"]
  }
}

resource "google_compute_route" "through-cloudbuild-nat1" {
  name        = "through-cloudbuild-nat1"
  dest_range  = "0.0.0.0/1"
  network     = google_compute_network.cloud_build_network.name
  next_hop_instance = google_compute_instance.cloud_build_nat_vm.self_link
  priority    = 1000
}

resource "google_compute_route" "through-cloudbuild-nat2" {
  name        = "through-cloudbuild-nat2"
  dest_range  = "128.0.0.0/1"
  network     = google_compute_network.cloud_build_network.name
  next_hop_instance = google_compute_instance.cloud_build_nat_vm.self_link
  priority    = 1000
}

resource "google_compute_route" "cloudbuild-direct-to-gateway1" {
  name        = "cloudbuild-direct-to-gateway1"
  dest_range  = "0.0.0.0/1"
  network     = google_compute_network.cloud_build_network.name
  next_hop_gateway = "default-internet-gateway"
  tags = ["direct-gateway-access"]
  priority    = 10
}

resource "google_compute_route" "cloudbuild-direct-to-gateway2" {
  name        = "cloudbuild-direct-to-gateway2"
  dest_range  = "128.0.0.0/1"
  network     = google_compute_network.cloud_build_network.name
  next_hop_gateway = "default-internet-gateway"
  tags = ["direct-gateway-access"]
  priority    = 10
}

resource "google_compute_firewall" "cloudbuild-allow-pool-to-nat" {
  name    = "cloudbuild-allow-ssh"
  network = google_compute_network.cloud_build_network.name

  direction = "INGRESS"
  allow {
    protocol = "all"
  }

  target_tags = ["nat-gateway"]
  source_ranges = ["10.2.0.0/24"]

  priority = 1000
}
