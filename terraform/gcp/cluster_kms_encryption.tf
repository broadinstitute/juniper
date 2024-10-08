resource "google_kms_key_ring" "juniper_cluster_keyring" {
  location = var.region
  name     = "juniper-cluster-keyring"

  depends_on = [
    time_sleep.enable_all_services_with_timeout
  ]
}

resource "google_kms_crypto_key" "juniper_cluster_crypto_key" {
  key_ring = google_kms_key_ring.juniper_cluster_keyring.id
  name     = "juniper-cluster-crypto-key"

  rotation_period = "7776000s" # 90 days

  lifecycle {
    prevent_destroy = true
  }
}
