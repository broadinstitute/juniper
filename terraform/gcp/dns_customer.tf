resource "google_dns_managed_zone" "customer_dns_zone" {
  for_each = var.customer_urls

  description = "${each.key} portal dns zone"
  dns_name    =  "${each.value.url}."
  dnssec_config {
    kind          = "dns#managedZoneDnsSecConfig"
    non_existence = "nsec3"
    state         = each.value.dnssec
  }
  name       = "juniper-dns-zone-${each.key}"
  visibility = "public"

  depends_on = [
    time_sleep.enable_all_services_with_timeout
  ]
}

resource "google_dns_record_set" "a_customer_url" {
  for_each = var.customer_urls

  managed_zone = google_dns_managed_zone.customer_dns_zone[each.key].name
  name         = google_dns_managed_zone.customer_dns_zone[each.key].dns_name
  rrdatas      = [google_compute_global_address.admin_ip_address.address]
  ttl          = var.dns_ttl
  type         = "A"
}

resource "google_dns_record_set" "www_customer_url" {
  for_each = var.customer_urls

  managed_zone = google_dns_managed_zone.customer_dns_zone[each.key].name
  name         = "www.${google_dns_managed_zone.customer_dns_zone[each.key].dns_name}"
  rrdatas      = [google_dns_record_set.a_customer_url[each.key].name]
  ttl          = var.dns_ttl
  type         = "CNAME"
}

# create sandbox, irb, live subdomains for the demo project
resource "google_dns_record_set" "sandbox_portal_customer_url" {
  for_each = var.customer_urls

  managed_zone = google_dns_managed_zone.customer_dns_zone[each.key].name

  name = "sandbox.${google_dns_managed_zone.customer_dns_zone[each.key].dns_name}"

  rrdatas      = [google_dns_record_set.a_customer_url[each.key].name]
  ttl          = var.dns_ttl
  type         = "CNAME"
}

resource "google_dns_record_set" "irb_portal_customer_url" {
  for_each = var.customer_urls

  managed_zone = google_dns_managed_zone.customer_dns_zone[each.key].name

  name = "irb.${google_dns_managed_zone.customer_dns_zone[each.key].dns_name}"

  rrdatas      = [google_dns_record_set.a_customer_url[each.key].name]
  ttl          = var.dns_ttl
  type         = "CNAME"
}
