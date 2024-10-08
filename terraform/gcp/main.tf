provider "google" {
  project     = var.project
  region      = var.region
}

# need to create some IAM binding to read artifact registry in infra project
provider "google" {
  project = var.infra_project
  region  = var.infra_region
  alias  = "infra"
}

data "google_client_config" "provider" {}

# state is stored remotely in GCS bucket
terraform {
  backend "gcs" {
    bucket = "broad-juniper-terraform-remote-state"
    prefix = "juniper"
  }
}
