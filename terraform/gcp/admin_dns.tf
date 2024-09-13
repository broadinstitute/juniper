# resource "google_dns_managed_zone" "juniper_admin_dns_zone" {
#   description = "<url>.org zone"
#   dns_name    = "<url>.org."
#   dnssec_config {
#     kind          = "dns#managedZoneDnsSecConfig"
#     non_existence = "nsec3"
#     state         = "off"
#   }
#   name       = "<url>-org"
#   visibility = "public"
# }
#
# resource "google_dns_record_set" "a_juniper_admin" {
#   managed_zone = google_dns_managed_zone.juniper_admin_dns_zone.name
#   name         = google_dns_managed_zone.juniper_admin_dns_zone.dns_name
#   rrdatas      = [] # todo
#   ttl          = var.dns_ttl
#   type         = "A"
# }
# resource "google_dns_record_set" "aaaa_juniper_admin" {
#   managed_zone = google_dns_managed_zone.juniper_admin_dns_zone.name
#   name         = google_dns_managed_zone.juniper_admin_dns_zone.dns_name
#   rrdatas      = [] # todo
#   ttl          = var.dns_ttl
#   type         = "AAAA"
# }
#
# resource "google_dns_record_set" "www_juniper_admin" {
#   managed_zone = google_dns_managed_zone.juniper_admin_dns_zone.name
#   name         = "www.${google_dns_managed_zone.juniper_admin_dns_zone.dns_name}"
#   rrdatas      = [] # todo
#   ttl          = var.dns_ttl
#   type         = "CNAME"
# }
