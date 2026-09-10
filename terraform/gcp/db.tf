# Creates the database used by Juniper as well as the user that will access it
# and secrets to allow the application to read the credentials.

locals {
  db_audit = var.enable_db_audit_logging ? "on" : "off"

  # The audit trail is a production requirement but the dominant source of Cloud
  # Logging ingestion, so it is switchable per environment. pgaudit stays
  # installed everywhere and is silenced via pgaudit.log instead: turning
  # cloudsql.enable_pgaudit off requires dropping the extension by hand first.
  db_flags = {
    "cloudsql.iam_authentication" = "on"
    "log_lock_waits"              = "on"
    "log_min_error_statement"     = "error"
    "log_min_messages"            = "warning"
    "cloudsql.enable_pgaudit"     = "on"
    "pgaudit.log"                 = var.enable_db_audit_logging ? "all" : "none"
    "log_connections"             = local.db_audit
    "log_disconnections"          = local.db_audit
    "log_hostname"                = local.db_audit
    "log_checkpoints"             = local.db_audit
  }
}

resource "google_sql_database_instance" "d2p" {
  name = "d2p"
  database_version = "POSTGRES_16"
  region           = var.region
  deletion_protection = false
  settings {
    tier = var.db_tier

    availability_type = var.db_availability_type
    ip_configuration {
      ipv4_enabled    = false
      private_network = google_compute_network.juniper_network.self_link
      enable_private_path_for_google_cloud_services = true
      ssl_mode = "TRUSTED_CLIENT_CERTIFICATE_REQUIRED"
    }

    dynamic "database_flags" {
      for_each = local.db_flags
      content {
        name  = database_flags.key
        value = database_flags.value
      }
    }

    backup_configuration {
      enabled = true
      start_time = "04:00"
      backup_retention_settings {
        retained_backups = 30
      }
    }
  }


  depends_on = [
    time_sleep.enable_all_services_with_timeout,
    google_service_networking_connection.private_vpc_connection
  ]
}

resource "google_sql_database" "database" {
  name     = "d2p"
  instance = google_sql_database_instance.d2p.name
}

resource "random_password" "random_db_password" {
  length           = 24
  special          = true
}

# Create database user
resource "google_sql_user" "users" {
  name     = "d2p"
  instance = google_sql_database_instance.d2p.name
  password = random_password.random_db_password.result
}




