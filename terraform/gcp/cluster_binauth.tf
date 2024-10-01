locals {
  whitelist_patterns = [
    "gcr.io/cloud-sql-connectors/*",
    "us-central1-docker.pkg.dev/broad-juniper-eng-infra/juniper/*",
    "us.gcr.io/broad-dsp-gcr-public/*"
  ]
}

resource "google_binary_authorization_policy" "juniper_cluster_binauth" {
  dynamic "admission_whitelist_patterns" {
    for_each = local.whitelist_patterns
    content {
      name_pattern = admission_whitelist_patterns.value
    }
  }

  # NOTE: all clusters in the project will be subject to this policy
  default_admission_rule {
    evaluation_mode         = "ALWAYS_DENY"
    enforcement_mode        = "ENFORCED_BLOCK_AND_AUDIT_LOG"
  }
}
