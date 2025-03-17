variable "project" {
  description = "The GCP project ID"
  type        = string
}

variable "region" {
  description = "The GCP region"
  type        = string
}

variable "virus_scanning_buckets" {
  type = set(object({
    unscanned: string,
    clean: string,
    quarantine: string
  }))
}

variable "virus_scanning_cvd_bucket_name" {
  type = string
}
