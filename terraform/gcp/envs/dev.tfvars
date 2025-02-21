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
documents_bucket_name = "juniper-participant-documents-dev"

# creates DNS records for these customer URLs
customer_urls = {
  demo = {
    url    = "juniperdemostudy.dev"
    dnssec = "on"
    additional_records = []
  }
}

slack_notification_channel = "projects/broad-juniper-dev/notificationChannels/13069356383599666729"
