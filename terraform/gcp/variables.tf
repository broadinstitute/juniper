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
  }
  default = "dev"
  description = "Environment (dev or prod)"
}

variable "juniper_folder_id" {
  type = string
  description = "Juniper folder ID"
}

variable "portals" {
  type = list(string)
  description = "Portals"
}
