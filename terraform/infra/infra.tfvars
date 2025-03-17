project = "broad-juniper-eng-infra"
region = "us-central1"

juniper_projects = ["broad-juniper-dev", "broad-juniper-prod"]
virus_scanning_cvd_bucket_name = "juniper-eng-infra-virus-scanning-cvd-mirror"
virus_scanning_buckets = [
  {
    clean: "juniper-participant-documents-dev-clean",
    unscanned: "juniper-participant-documents-dev-unscanned",
    quarantined: "juniper-participant-documents-dev-quarantined"
  },
  # {
  #   clean: "juniper-participant-documents-prod-clean",
  #   unscanned: "juniper-participant-documents-prod-unscanned",
  #   quarantined: "juniper-participant-documents-prod-quarantined"
  # },
]

malware_scanner_image_name = "juniper-malware-scanner"
