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
