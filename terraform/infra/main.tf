provider "google" {
  project     = var.project
  region      = var.region
}

terraform {
  backend "gcs" {
    bucket = "broad-juniper-terraform-remote-state"
    prefix = "infra"
  }
}
