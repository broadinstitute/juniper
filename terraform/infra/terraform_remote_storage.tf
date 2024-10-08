
resource "google_storage_bucket" "terraform_remote_state" {
  name     = "broad-juniper-terraform-remote-state"
  location = var.region

  force_destroy               = false

  # no public access allowed
  public_access_prevention    = "enforced"

  # only allow access if you have iam perms
  uniform_bucket_level_access = true

  project = var.project
  versioning {
    enabled = true
  }

  depends_on = [google_project_service.enable_bucket_storage]
}
