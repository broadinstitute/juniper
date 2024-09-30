# Create database
resource "google_sql_database_instance" "d2p" {
  name = "d2p"
  database_version = "POSTGRES_16"
  region           = var.region
  deletion_protection = false
  settings {
    tier = var.db_tier
    ip_configuration {
      ipv4_enabled    = false
      private_network = google_compute_network.juniper_network.self_link
      enable_private_path_for_google_cloud_services = true
    }

    database_flags {
      name  = "cloudsql.iam_authentication"
      value = "on"
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




