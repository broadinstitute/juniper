project = "broad-juniper-dev"
region = "us-central1"
db_tier = "db-f1-micro"
dns_ttl = 300
admin_url = "juniper-cmi.dev"
environment = "dev"
# note: automatically creates DNS records for these portals under the admin domain
portals = ["demo"]
infra_project = "broad-juniper-eng-infra"
infra_region = "us-central1"
