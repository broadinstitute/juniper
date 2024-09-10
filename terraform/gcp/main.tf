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
  project     = "broad-ddp-dev"
  region      = "us-central1"
}

data "google_client_config" "provider" {}

data "google_container_cluster" "juniper_cluster" {
  name     = "juniper-test-cluster-2"
  location = "us-central1"
}

provider "kubernetes" {
  host  = "https://${data.google_container_cluster.juniper_cluster.endpoint}"
  token = data.google_client_config.provider.access_token
  cluster_ca_certificate = base64decode(
    data.google_container_cluster.juniper_cluster.master_auth[0].cluster_ca_certificate,
  )
}
