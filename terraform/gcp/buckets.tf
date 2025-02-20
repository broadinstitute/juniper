resource "google_storage_bucket" "participant_documents" {
  name     = var.documents_bucket_name
  location = var.region

  versioning {
    enabled = true
  }
}
