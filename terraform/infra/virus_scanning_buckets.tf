resource "google_storage_bucket" "virus_cvd_bucket"{
  location = var.region
  name = var.virus_scanning_cvd_bucket_name

  uniform_bucket_level_access = true
  public_access_prevention = true
}


data "google_storage_bucket" "unscanned_buckets" {
  for_each = var.virus_scanning_buckets

  name = each.value.unscanned
}

data "google_storage_bucket" "clean_buckets" {
  for_each = var.virus_scanning_buckets

  name = each.value.clean
}

data "google_storage_bucket" "quarantined_buckets" {
  for_each = var.virus_scanning_buckets

  name = each.value.quarantine
}

