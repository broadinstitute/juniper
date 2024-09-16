resource "google_compute_global_address" "admin_ip_address" {
  name = "admin-ip"
}

resource "google_dns_managed_zone" "juniper_admin_dns_zone" {
  description = "juniper-admin-dns-zone"
  dns_name    = var.admin_url + "."
  dnssec_config {
    kind          = "dns#managedZoneDnsSecConfig"
    non_existence = "nsec3"
    state         = "off"
  }
  name       = "juniper-admin-dns-zone"
  visibility = "public"
}

resource "google_dns_record_set" "a_juniper_admin" {
  managed_zone = google_dns_managed_zone.juniper_admin_dns_zone.name
  name         = google_dns_managed_zone.juniper_admin_dns_zone.dns_name
  rrdatas      = [google_compute_global_address.admin_ip_address.address]
  ttl          = var.dns_ttl
  type         = "A"
}

resource "google_dns_record_set" "www_juniper_admin" {
  managed_zone = google_dns_managed_zone.juniper_admin_dns_zone.name
  name         = "www.${google_dns_managed_zone.juniper_admin_dns_zone.dns_name}"
  rrdatas      = [google_dns_record_set.a_juniper_admin.name]
  ttl          = var.dns_ttl
  type         = "CNAME"
}

# create sandbox, irb, live subdomains for the demo project
resource "google_dns_record_set" "sandbox_demo_juniper_admin" {
  managed_zone = google_dns_managed_zone.juniper_admin_dns_zone.name

  # create a cname for each environment
  name = ""
  dynamic "environment" {
    for_each = ["sandbox", "irb", "live"]
    content {
      dynamic "portal" {
        for_each = var.portals
        content {
          name = "${environment}.${portal}.${google_dns_managed_zone.juniper_admin_dns_zone.dns_name}"
        }
      }
    }
  }

  rrdatas      = [google_dns_record_set.a_juniper_admin.name]
  ttl          = var.dns_ttl
  type         = "CNAME"
}
