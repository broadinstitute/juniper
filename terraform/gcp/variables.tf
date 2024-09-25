variable "project" {
  type = string
  default = ""
  description = "GCP project"
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

variable "dns_ttl" {
  type = number
  default = 300
  description = "DNS TTL"
}

variable "admin_url" {
  type = string
  description = "Admin URL"
}


variable "environment" {
  type = string
  validation {
    condition = can(regex("^(dev|prod)$", var.environment))
    error_message = "must be dev or prod"
  }
  default = "dev"
  description = "Environment (dev or prod)"
}

variable "portals" {
  type = set(string)
  description = "Portals"
}

variable "infra_project" {
  type = string
  description = "Infra project"
}

variable "infra_region" {
  type = string
  description = "Infra region"
}

variable "authorized_networks" {
  type = set(string)
  description = "Authorized networks that can access the cluster"
}
