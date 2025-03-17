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
    quarantined: string
  }))
}

variable "juniper_projects" {
  type = set(string)
}

variable "virus_scanning_cvd_bucket_name" {
  type = string
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
