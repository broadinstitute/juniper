/**
 * # D2P
 *
 * This module creates the Azure cloud resources needed to run an instance of DDP's new Arbor Application
 *
 * This documentation is generated with [terraform-docs](https://github.com/segmentio/terraform-docs)
 * `terraform-docs markdown --no-sort . > README.md`
 */


# Resource group used as a container for all resources associated with the cluster
# This offers another layer of organizing resources within a subscription (analogue to gcp project)
# There is no direct analog in gcp to a resource group but it provides a logical container for all
# resources associated with the cluster

provider "google" {
  project     = var.project
  region      = var.region
}

data "google_client_config" "provider" {}

# data "google_container_cluster" "juniper_cluster" {
#   name     = "juniper-cluster"
#   location = var.region
# }

provider "kubernetes" {
  host  = "https://${google_container_cluster.juniper_cluster.endpoint}"
  token = data.google_client_config.provider.access_token
  cluster_ca_certificate = base64decode(
    google_container_cluster.juniper_cluster.master_auth[0].cluster_ca_certificate,
  )
}
