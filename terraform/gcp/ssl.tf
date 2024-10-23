resource "google_compute_ssl_policy" "juniper-ssl-policy" {
  name    = "juniper-ssl-policy"
  profile = "MODERN"
  min_tls_version = "TLS_1_2"
}
