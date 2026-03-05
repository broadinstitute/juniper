project = "broad-juniper-prod"
project_number = 849235144342
region = "us-central1"
db_tier = "db-custom-2-7680" # 2 vCPUs, 7.5 GB RAM
db_availability_type = "REGIONAL" # makes database highly available by replicating data across multiple zones
dns_ttl = 300
admin_url = "juniper-cmi.org"
environment = "prod"
# note: automatically creates DNS records for these portals under the admin domain

# TODO: remove pedihcc next release
portals = ["demo", "atcp", "ourhealth", "hearthive", "rgp", "cmi", "trccproject", "gvasc", "pedihccproject", "pedihcc"]

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
        name = "_mx"
        domain_prefix = ""
        type = "MX"
        ttl = 3600
        record_values = ["1 thehearthive-org.mail.protection.outlook.com."]
      },
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
          name = "autodiscover"
          domain_prefix = "autodiscover"
          type = "CNAME"
          ttl = 3600
          record_values = ["autodiscover.outlook.com."]
      },
        {
            name = "selector1._domainkey"
            domain_prefix = "selector1._domainkey"
            type = "CNAME"
            ttl = 3600
            record_values = ["selector1-thehearthive-org._domainkey.ImperialLondon.r-v1.dkim.mail.microsoft."]
          },
       {
          name = "selector2._domainkey"
          domain_prefix = "selector2._domainkey"
          type = "CNAME"
          ttl = 3600
          record_values = ["selector2-thehearthive-org._domainkey.ImperialLondon.r-v1.dkim.mail.microsoft."]
        },
      {
        name = "_dmarc"
        domain_prefix = "_dmarc"
        type = "TXT"
        ttl = 3600
        record_values = ["v=DMARC1;p=none;rua=mailto:dmarc_agg@vali.email;"]
      },
      {
          name = "_spf"
          domain_prefix = ""
          type = "TXT"
          ttl = 3600
          record_values = ["v=spf1 include:spf.protection.outlook.com -all;"]
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
      {
        name = "gsuite_verification"
        domain_prefix = ""
        type = "TXT"
        ttl = 3600
        record_values = [
          "google-site-verification=Vs56YsgRkHl4DxIqUfHYD69HbUUTQLLnPFWZbA3QVXM",
          "v=spf1 include:46085939.spf01.hubspotemail.net -all"
        ]
      },
      {
        name = "dmarc",
        domain_prefix="_dmarc",
        type="TXT",
        ttl=3600,
        record_values = [
          "v=DMARC1;p=none;"
        ]
      },
      {
        name="domainkey1",
        domain_prefix="hs1-46085939._domainkey",
        type="CNAME",
        ttl=3600,
        record_values=[
          "trccproject-org.hs16a.dkim.hubspotemail.net."
        ]
      },
      {
        name="domainkey2",
        domain_prefix="hs2-46085939._domainkey",
        type="CNAME",
        ttl=3600,
        record_values=[
          "trccproject-org.hs16b.dkim.hubspotemail.net."
        ]
      }
    ]
  }
  pedihccproject = {
    url    = "pedihccproject.org"
    dnssec = "off"
    additional_records = []
  }
  atcp = {
    url    = "atfamilies.org"
    dnssec = "off"
    additional_records = [
      {
        name = "mx_record"
        domain_prefix = ""
        type = "MX"
        ttl = 3600
        record_values = [
          "1 aspmx.l.google.com.",
          "5 alt1.aspmx.l.google.com.",
          "5 alt2.aspmx.l.google.com.",
          "10 aspmx2.googlemail.com.",
          "10 aspmx3.googlemail.com."
        ]
      },
      {
        name = "gsuite_verification"
        domain_prefix = ""
        type = "TXT"
        ttl = 3600
        record_values = [
          "google-site-verification=CeH80RLBV1kKpmgIrPmp_z8Wcpuj4010PuRjtXG2CQo",
          "google-site-verification=-ITHj3d7lESwMh9-cKntUBYlAZ9nnXekIMpBBnvCWVU",
          "v=spf1 include:_spf.google.com ~all"
        ]
      },
      {
        name="sendgrid"
        domain_prefix="em1453"
        type="CNAME"
        ttl=3600
        record_values=["u33588015.wl016.sendgrid.net."]
      },
      {
        name="sendgrid_domainkey1"
        domain_prefix="j._domainkey"
        type="CNAME"
        ttl=3600
        record_values=["j.domainkey.u33588015.wl016.sendgrid.net."]
      },
      {
        name="sendgrid_domainkey2"
        domain_prefix="j2._domainkey"
        type="CNAME"
        ttl=3600
        record_values=["j2.domainkey.u33588015.wl016.sendgrid.net."]
      },
      {
        name="dmarc"
        domain_prefix="_dmarc"
        type="TXT"
        ttl=3600
        record_values=["v=DMARC1;p=none;"]
      }
    ]
  }
}

slack_notification_channel = "projects/broad-juniper-prod/notificationChannels/9072110396476167224"

malware_scanner_image_name = "juniper-malware-scanner"
documents_bucket_name = "juniper-participant-documents-prod"
