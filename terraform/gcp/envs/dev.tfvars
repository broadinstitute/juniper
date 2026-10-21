project = "broad-juniper-dev"
project_number = 663573365422
region = "us-central1"
db_tier = "db-g1-small"
dns_ttl = 300
admin_url = "juniper-cmi.dev"
environment = "dev"
# note: automatically creates DNS records for these portals under the admin domain
portals = ["demo", "atcp", "ourhealth", "hearthive", "rgp", "cmi"]
k8s_namespace = "juniper-dev"

malware_scanner_image_name = "juniper-malware-scanner"
documents_bucket_name = "juniper-participant-documents-dev"

# cost controls for this environment -- it sees occasional manual testing only.
# scale the scanner to zero between tests; the first scan afterwards pays a
# ClamAV cold start (the startup probe already allows up to 240s for it)
malware_scanner_min_instances = 0
malware_scanner_max_instances = 2
malware_scanner_cpu_idle = true

# skip the pgaudit trail and VPC flow logs; both are prod audit requirements
# and together they dominated this project's Cloud Logging bill
enable_db_audit_logging = false
enable_flow_logs = false

# only NATs cloud build traffic to the GKE control plane during deploys
cloud_build_nat_machine_type = "e2-small"

# creates DNS records for these customer URLs
customer_urls = {
  demo = {
    url    = "juniperdemostudy.dev"
    dnssec = "on"
    additional_records = []
  }
}

slack_notification_channel = "projects/broad-juniper-dev/notificationChannels/13069356383599666729"

