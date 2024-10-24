project = "broad-juniper-prod"
project_number = 849235144342
region = "us-central1"
db_tier = "db-custom-2-7680" # 2 vCPUs, 7.5 GB RAM
db_availability_type = "REGIONAL" # makes database highly available by replicating data across multiple zones
dns_ttl = 300
admin_url = "juniper-cmi.org"
environment = "prod"
# note: automatically creates DNS records for these portals under the admin domain
portals = ["demo"]
infra_project = "broad-juniper-eng-infra"
infra_region = "us-central1"
admin_dnssec = "off"
k8s_namespace = "juniper-prod"

# creates DNS records for these customer URLs
customer_urls = {
  demo = {
    url    = "juniperdemostudy.org"
    dnssec = "off"
  }
}
