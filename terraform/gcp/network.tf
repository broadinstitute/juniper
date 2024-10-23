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
  private_ipv6_google_access = "ENABLE_GOOGLE_ACCESS"

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

resource "google_compute_security_policy" "juniper-cloud-armor-policy" {
  name = "juniper-cloud-armor-policy"

  rule {
    action   = "allow"
    description = "Default rule: allow all"
    priority = 2147483647

    match {
      versioned_expr = "SRC_IPS_V1"
      config {
        src_ip_ranges = ["*"]
      }
    }
  }

  rule {
    action = "deny(502)"
    priority = 7555
    match {
      expr {
        expression = "evaluatePreconfiguredWaf('rce-v33-stable', {'sensitivity':1}) || evaluatePreconfiguredWaf('rfi-v33-stable', {'sensitivity':1}) || evaluatePreconfiguredWaf('sessionfixation-v33-stable', {'sensitivity':1}) || evaluatePreconfiguredWaf('sqli-v33-stable', {'sensitivity':1}) || evaluatePreconfiguredWaf('xss-v33-stable', {'sensitivity':1})"
      }
    }
  }

  rule {
    action = "deny(502)"
    priority = 7444
    match {
      expr {
        expression = "evaluatePreconfiguredWaf('java-v33-stable', {'sensitivity':1}) || evaluatePreconfiguredWaf('lfi-v33-stable', {'sensitivity':1}) || evaluatePreconfiguredWaf('nodejs-v33-stable', {'sensitivity':1}) || evaluatePreconfiguredWaf('php-v33-stable', {'sensitivity':1}) || evaluatePreconfiguredWaf('protocolattack-v33-stable', {'sensitivity':1})"
      }
    }
  }

  rule {
    action = "deny(502)"
    priority = 7111
    match {
      expr {
        expression = "evaluatePreconfiguredWaf('cve-canary', {'sensitivity':1}) || evaluatePreconfiguredWaf('rfi-v33-stable', {'sensitivity':1}) || evaluatePreconfiguredWaf('scannerdetection-v33-stable', {'sensitivity':1})"
      }
    }
  }
}
