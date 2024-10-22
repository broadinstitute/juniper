project = "broad-juniper-dev"
project_number = 663573365422
region = "us-central1"
db_tier = "db-g1-small"
dns_ttl = 300
admin_url = "juniper-cmi.dev"
environment = "dev"
# note: automatically creates DNS records for these portals under the admin domain
portals = ["demo", "atcp", "ourhealth", "hearthive", "rgp", "cmi"]
infra_project = "broad-juniper-eng-infra"
infra_region = "us-central1"

# creates DNS records for these customer URLs
customer_urls = {
  demo = {
    url    = "juniperdemostudy.dev"
    dnssec = "off"
  }
}
