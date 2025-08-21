# Creates DNS records for all customer URLs.

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

resource "google_dns_record_set" "caa_customer_url" {
  for_each = var.customer_urls

  name = google_dns_managed_zone.customer_dns_zone[each.key].dns_name
  type = "CAA"
  ttl  = 300
  managed_zone = google_dns_managed_zone.customer_dns_zone[each.key].name
  rrdatas = ["0 issue \"letsencrypt.org\"", "0 issue \"pki.goog\""]
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

resource "google_dns_record_set" "additional_customer_records" {
  # for each customer, create a record for each additional record (e.g. sendgrid, dmarc, etc.)
  for_each = {
      for index, item in flatten([
        for customer_key, dns_config in var.customer_urls : [
          for dns_record in dns_config.additional_records : {
            customer_key        = customer_key
            name   = dns_record.name
            type   = dns_record.type
            ttl    = dns_record.ttl
            record_value  = dns_record.record_value
        }
        ]
      ]) : "${item.customer_key}.${item.name}" => item # for_each expects maps, so convert the list of objects to a map
  }

  managed_zone = google_dns_managed_zone.customer_dns_zone[each.value.customer_key].name
  name = (each.value.name == ""
            ? google_dns_managed_zone.customer_dns_zone[each.value.customer_key].dns_name
            : "${each.value.name}.${google_dns_managed_zone.customer_dns_zone[each.value.customer_key].dns_name}")
  type         = each.value.type
  rrdatas      = [each.value.record_value]
  ttl          = each.value.ttl
}
