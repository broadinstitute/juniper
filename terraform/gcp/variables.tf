variable "project" {
  type = string
  default = ""
  description = "GCP project"
}

variable "project_number" {
  type = number
  description = "GCP project number"
}

variable "region" {
  type = string
  default = "us-central1"
  description = "GCP location"
}

variable "db_tier" {
  type = string
  # for production, use machine type from https://cloud.google.com/sql/docs/postgres/instance-settings
  default = "db-f1-micro"
  description = "Database tier"
}

variable "db_availability_type" {
  type = string
  default = "ZONAL"
  description = "Database availability type"
  validation {
    condition = can(regex("^(ZONAL|REGIONAL)$", var.db_availability_type))
    error_message = "must be ZONAL or REGIONAL"
  }
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

variable "customer_urls" {
  type = map(object({
    url = string
    dnssec = string
  }))
  description = "Customer URLs"
}

variable "infra_project" {
  type = string
  description = "Infra project"
}

variable "infra_region" {
  type = string
  description = "Infra region"
}

variable "admin_dnssec" {
    type = string
    default = "on"
    description = "Admin DNSSEC"
    validation {
        condition = can(regex("^(on|off)$", var.admin_dnssec))
        error_message = "must be on or off"
    }
}

variable "k8s_namespace" {
  type = string
  description = "Kubernetes namespace"
  default = "juniper"
}
