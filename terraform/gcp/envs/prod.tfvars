project = "broad-juniper-prod"
project_number = 849235144342
region = "us-central1"
db_tier = "db-custom-2-7680" # 2 vCPUs, 7.5 GB RAM
db_availability_type = "REGIONAL" # makes database highly available by replicating data across multiple zones
dns_ttl = 300
admin_url = "juniper-cmi.org"
environment = "prod"
# note: automatically creates DNS records for these portals under the admin domain

portals = ["demo", "atcp", "ourhealth", "hearthive", "rgp", "cmi", "trccproject", "gvasc"]

admin_dnssec = "on"
k8s_namespace = "juniper-prod"

# creates DNS records for these customer URLs
customer_urls = {
  demo = {
    url    = "juniperdemostudy.org"
    dnssec = "on"
    additional_records = []
  }
  hearthive = {
    url    = "thehearthive.org"
    dnssec = "on"
    additional_records = [
      {
        name = "s1._domainkey"
        domain_prefix = "s1._domainkey"
        type = "CNAME"
        ttl = 3600
        record_values = ["s1.domainkey.u33588015.wl016.sendgrid.net."]
      },
      {
        name = "s2._domainkey"
        domain_prefix = "s2._domainkey"
        type = "CNAME"
        ttl = 3600
        record_values = ["s2.domainkey.u33588015.wl016.sendgrid.net."]
      },
      {
        name = "em6454"
        domain_prefix = "em6454"
        type = "CNAME"
        ttl = 3600
        record_values = ["u33588015.wl016.sendgrid.net."]
      },
      {
        name = "url9076"
        domain_prefix = "url9076"
        type = "CNAME"
        ttl = 3600
        record_values = ["sendgrid.net."]
      },
      {
        name = "_dmarc"
        domain_prefix = "_dmarc"
        type = "TXT"
        ttl = 3600
        record_values = ["v=DMARC1;p=none;"]
      },
      {
        name = "33588015"
        domain_prefix = "33588015"
        type = "CNAME"
        ttl = 3600
        record_values = ["sendgrid.net."]
      }
    ]
  }
  ourhealth = {
    url    = "ourhealthstudy.org"
    dnssec = "on"
    additional_records = [
      {
        name = "s1._domainkey"
        domain_prefix = "s1._domainkey"
        type = "CNAME"
        ttl = 3600
        record_values = ["s1.domainkey.u33588015.wl016.sendgrid.net."]
      },
      {
        name = "s2._domainkey"
        domain_prefix = "s2._domainkey"
        type = "CNAME"
        ttl = 3600
        record_values = ["s2.domainkey.u33588015.wl016.sendgrid.net."]
      },
      {
        name = "em1287"
        domain_prefix = "em1287"
        type = "CNAME"
        ttl = 3600
        record_values = ["u33588015.wl016.sendgrid.net."]
      },
      {
        name = "em1800"
        domain_prefix = "em1800"
        type = "CNAME"
        ttl = 3600
        record_values = ["u32431094.wl095.sendgrid.net."]
      }
    ]
  }
  gvasc = {
    url    = "gvascstudy.org"
    dnssec = "on"
    additional_records = []
  }
  trcc = {
    url    = "trccproject.org"
    dnssec = "off"
    additional_records = [
      {
        name = "mx_record"
        domain_prefix = ""
        type = "MX"
        ttl = 3600
        record_values = ["1 smtp.google.com."]
      },
    ]
  }
}

slack_notification_channel = "projects/broad-juniper-prod/notificationChannels/9072110396476167224"

malware_scanner_image_name = "juniper-malware-scanner"
documents_bucket_name = "juniper-participant-documents-prod"
