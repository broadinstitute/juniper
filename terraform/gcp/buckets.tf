resource "google_storage_bucket" "participant_documents" {
  name     = var.documents_bucket_name
  location = var.region
  # no public access allowed
  public_access_prevention    = "enforced"

  # only allow access if you have iam perms
  uniform_bucket_level_access = true
  versioning {
    enabled = true
  }
}
