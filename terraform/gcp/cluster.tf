locals {
  # authorize access only to broad networks
  authorized_networks = [
    "69.173.64.0/19",
    "69.173.96.0/24",
    "69.173.97.0/25",
    "69.173.97.128/26",
    "69.173.97.192/27",
    "69.173.98.0/23",
    "69.173.100.0/22",
    "69.173.104.0/22",
    "69.173.108.0/22",
    "69.173.112.0/21",
    "69.173.120.0/22",
    "69.173.124.0/23",
    "69.173.126.0/24",
    "69.173.127.0/25",
    "69.173.127.128/26",
    "69.173.127.192/27",
    "69.173.127.240/28"
  ]
}

resource "google_container_cluster" "juniper_cluster" {
  name = "juniper-cluster"

  location                 = var.region
  enable_autopilot         = true
  enable_l4_ilb_subsetting = true

  network    = google_compute_network.juniper_network.id
  subnetwork = google_compute_subnetwork.juniper_subnetwork.id

  binary_authorization {
    evaluation_mode = "PROJECT_SINGLETON_POLICY_ENFORCE"
  }

  ip_allocation_policy {
    stack_type = "IPV4_IPV6"
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
      for_each = local.authorized_networks
      content {
        cidr_block   = cidr_blocks.value
        display_name = cidr_blocks.value
      }
    }
  }

  master_auth {
    client_certificate_config {
      issue_client_certificate = false
    }
  }

  database_encryption {
    key_name = google_kms_crypto_key.juniper_cluster_crypto_key.id
    state    = "ENCRYPTED"
  }

  release_channel {
    channel = "REGULAR"
  }

  # Set `deletion_protection` to `true` will ensure that one cannot
  # accidentally delete this instance by use of Terraform.
  deletion_protection = false

  depends_on = [
    time_sleep.enable_all_services_with_timeout,
    google_kms_key_ring_iam_binding.cluster-key-ring
  ]
}

