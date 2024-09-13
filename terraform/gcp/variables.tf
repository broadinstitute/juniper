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
