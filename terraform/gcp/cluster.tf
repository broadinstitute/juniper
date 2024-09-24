resource "google_container_cluster" "juniper_cluster" {
  name = "juniper-cluster"

  location                 = var.region
  enable_autopilot         = true
  enable_l4_ilb_subsetting = true

  network    = google_compute_network.juniper_network.id
  subnetwork = google_compute_subnetwork.juniper_subnetwork.id

  ip_allocation_policy {
    stack_type                    = "IPV4_IPV6"
  }

  cluster_autoscaling {
    auto_provisioning_defaults {
      service_account = google_service_account.cluster_service_account.email
    }
  }

  private_cluster_config {
    enable_private_nodes    = true
  }

  master_authorized_networks_config {
    dynamic "cidr_blocks" {
      for_each = var.authorized_networks
      content {
        cidr_block   = cidr_blocks.value
        display_name = cidr_blocks.value
      }
    }
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

