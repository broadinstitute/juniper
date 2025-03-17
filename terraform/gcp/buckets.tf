resource "google_storage_bucket" "unscanned_participant_documents" {
  name     = "${var.documents_bucket_name}-unscanned"
  location = var.region
  # no public access allowed
  public_access_prevention    = "enforced"

  # only allow access if you have iam perms
  uniform_bucket_level_access = true
  versioning {
    enabled = true
  }
}

resource "google_storage_bucket" "clean_participant_documents" {
  name     = "${var.documents_bucket_name}-clean"
  location = var.region
  # no public access allowed
  public_access_prevention    = "enforced"

  # only allow access if you have iam perms
  uniform_bucket_level_access = true
  versioning {
    enabled = true
  }
}

resource "google_storage_bucket" "quarantined_participant_documents" {
  name     = "${var.documents_bucket_name}-quarantined"
  location = var.region
  # no public access allowed
  public_access_prevention    = "enforced"

  # only allow access if you have iam perms
  uniform_bucket_level_access = true
  versioning {
    enabled = true
  }
}

resource "google_storage_bucket" "cvd_mirror_bucket" {
    name     = "${var.documents_bucket_name}-cvd-mirror"
    location = var.region
    # no public access allowed
    public_access_prevention    = "enforced"

    # only allow access if you have iam perms
    uniform_bucket_level_access = true
    versioning {
        enabled = true
    }
}
