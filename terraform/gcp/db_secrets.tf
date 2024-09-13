# create google secret manager db secrets

resource "google_secret_manager_secret" "d2p_db_user" {
  secret_id = "d2p-db-user"
  replication {
    auto {}
  }
}

resource "google_secret_manager_secret_version" "d2p_db_user_data" {
  secret = google_secret_manager_secret.d2p_db_user.id

  secret_data = google_sql_user.users.name
}

resource "google_secret_manager_secret" "d2p_db_name" {
  secret_id = "d2p-db-name"
  replication {
    auto {}
  }
}

resource "google_secret_manager_secret_version" "d2p_db_name_data" {
  secret = google_secret_manager_secret.d2p_db_name.id

  secret_data = google_sql_database.database.name
}

resource "google_secret_manager_secret" "d2p_db_password" {
  secret_id = "d2p-db-password"
  replication {
    auto {}
  }
}

resource "google_secret_manager_secret_version" "d2p_db_password_data" {
  secret = google_secret_manager_secret.d2p_db_password.id

  secret_data = google_sql_user.users.password
}
