# Create database
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

    database_flags {
      name  = "cloudsql.iam_authentication"
      value = "on"
    }
    database_flags {
      name  = "log_checkpoints"
      value = "on"
    }
    database_flags {
      name  = "log_lock_waits"
      value = "on"
    }
    database_flags {
      name  = "log_connections"
      value = "on"
    }
    database_flags {
      name  = "log_disconnections"
      value = "on"
    }
    database_flags {
      name  = "log_hostname"
      value = "on"
    }
    database_flags {
      name  = "log_min_error_statement"
      value = "error"
    }
    database_flags {
      name  = "log_min_messages"
      value = "warning"
    }
    database_flags {
      name  = "cloudsql.enable_pgaudit"
      value = "on"
    }
    database_flags {
      name  = "pgaudit.log"
      value = "all"
    }

    backup_configuration {
      enabled = true
      start_time = "04:00"
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




