resource "google_sql_database_instance" "d2p" {
  name = "d2p"
  database_version = "POSTGRES_15"
  region           = "us-central1"
  deletion_protection = false
  settings {
    tier = "db-f1-micro"
  }
}

resource "google_sql_user" "users" {
  name     = "d2p"
  instance = google_sql_database_instance.d2p.name
  password = "d2p4eva!"
}

resource "google_sql_database" "database" {
  name     = "d2p"
  instance = google_sql_database_instance.d2p.name
}

