project = "broad-juniper-dev"
region = "us-central1"
db_tier = "db-f1-micro"
dns_ttl = 300
admin_url = "juniper-cmi.dev"
juniper_folder_id = "272674505704"
environment = "dev"
# note: automatically creates DNS records for these portals under the admin domain
portals = ["demo", "ourhealth", "hearthive", "gvasc", "cmitemplate"]
