variable "project" {
  type = string
  default = ""
  description = "GCP project"
}

variable "cluster" {
  type = string
  default = "juniper"
  description = "GKE cluster name"
}

variable "region" {
  type = string
  default = "us-central1"
  description = "GCP location"
}

variable "db_tier" {
  type = string
  default = "db-f1-micro"
  description = "Database tier"
}
