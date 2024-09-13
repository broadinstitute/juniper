# Create database
resource "google_sql_database_instance" "d2p" {
  name = "d2p"
  database_version = "POSTGRES_15"
  region           = var.region
  deletion_protection = false
  settings {
    tier = var.db_tier

    database_flags {
      name  = "cloudsql.iam_authentication"
      value = "on"
    }
  }
}

resource "google_sql_database" "database" {
  name     = "d2p"
  instance = google_sql_database_instance.d2p.name
}

resource "random_string" "random_db_password" {
  length           = 24
  special          = true
}

# Create database user
resource "google_sql_user" "users" {
  name     = "d2p"
  instance = google_sql_database_instance.d2p.name
  password = random_string.random_db_password.result
}




