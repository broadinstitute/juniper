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
    additional_records = list(object({
      name = string
      domain_prefix = string
      type = string
      ttl = number
      record_values = list(string)
    }))
  }))
  description = "Customer URLs"
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

variable "slack_notification_channel" {
  type = string
  default = ""
  description = "Slack notification channel"
}

variable "documents_bucket_name" {
  type = string
  description = "The name of the GCP bucket for storing participant documents"
}

variable "artifact_registry" {
  type = string
  default = "juniper"
}

variable "artifact_registry_project" {
  type = string
  default = "broad-juniper-eng-infra"
}

variable "artifact_registry_location" {
  type = string
  default = "us-central1"
}


# build image from https://github.com/GoogleCloudPlatform/docker-clamav-malware-scanner/tree/main/cloudrun-malware-scanner
# steps:
# cd cloudrun-malware-scanner
# docker build --tag=us-central1-docker.pkg.dev/broad-juniper-eng-infra/juniper/juniper-malware-scanner:latest -f Dockerfile .  --platform linux/amd64
# docker push us-central1-docker.pkg.dev/broad-juniper-eng-infra/juniper/juniper-malware-scanner:latest

# if standing up for first time, you might also need to update the cvd mirror
# before deploying. run:
# pip3 install crcmod cvdupdate
# ./updateCvdMirror.sh  <cvd_mirror_bucket_name>
# from https://github.com/GoogleCloudPlatform/docker-clamav-malware-scanner

variable "malware_scanner_image_name" {
  type = string
}
