resource "google_storage_bucket" "cvd_mirror_bucket" {
  name     = var.virus_scanning_cvd_bucket_name
  location = var.region
  # no public access allowed
  public_access_prevention    = "enforced"

  # only allow access if you have iam perms
  uniform_bucket_level_access = true
  versioning {
    enabled = true
  }
}


data "google_storage_bucket" "unscanned_buckets" {
  for_each = {
    for index, buckets in var.virus_scanning_buckets:
        buckets.unscanned => buckets
  }

  name = each.value.unscanned
}

data "google_storage_bucket" "clean_buckets" {
  for_each = {
    for index, buckets in var.virus_scanning_buckets:
    buckets.unscanned => buckets
  }

  name = each.value.clean
}

data "google_storage_bucket" "quarantined_buckets" {
  for_each = {
    for index, buckets in var.virus_scanning_buckets:
    buckets.unscanned => buckets
  }

  name = each.value.quarantined
}

