project = "broad-juniper-dev"
project_number = 663573365422
region = "us-central1"
db_tier = "db-g1-small"
dns_ttl = 300
admin_url = "juniper-cmi.dev"
environment = "dev"
# note: automatically creates DNS records for these portals under the admin domain
portals = ["demo"]
infra_project = "broad-juniper-eng-infra"
infra_region = "us-central1"

# authorize access only to broad networks
authorized_networks = [
  "69.173.64.0/19",
  "69.173.96.0/24",
  "69.173.97.0/25",
  "69.173.97.128/26",
  "69.173.97.192/27",
  "69.173.98.0/23",
  "69.173.100.0/22",
  "69.173.104.0/22",
  "69.173.108.0/22",
  "69.173.112.0/21",
  "69.173.120.0/22",
  "69.173.124.0/23",
  "69.173.126.0/24",
  "69.173.127.0/25",
  "69.173.127.128/26",
  "69.173.127.192/27",
  "69.173.127.240/28"
]
