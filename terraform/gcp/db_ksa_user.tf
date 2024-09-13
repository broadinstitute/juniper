
# Create GSA and assign roles

resource "google_service_account" "d2p-db-gsa" {
  account_id = "d2p-db-gsa"
}

resource "google_sql_user" "d2p_db_service_account_user" {
  # Note: for PostgreSQL only, Google Cloud requires that you omit the
  # ".gserviceaccount.com" suffix
  # from the service account email due to length limits on database usernames.
  name     = trimsuffix(google_service_account.d2p-db-gsa.email, ".gserviceaccount.com")
  instance = google_sql_database_instance.d2p.name
  type     = "CLOUD_IAM_SERVICE_ACCOUNT"
}

resource "google_project_iam_binding" "access-postgres" {
  project = var.project
  role    = "roles/cloudsql.client"
  members = [
    "serviceAccount:${google_service_account.d2p-db-gsa.email}"
  ]
}

resource "google_project_iam_binding" "access-secrets" {
  project = var.project
  role    = "roles/secretmanager.secretAccessor"
  members = [
    "serviceAccount:${google_service_account.d2p-db-gsa.email}"
  ]
}


# Create KSA + workload identity to link it to GSA with DB access

resource "kubernetes_service_account" "d2p-db-ksa" {
  metadata {
    name      = "d2p-db-ksa"
    namespace = "default"
    annotations = {
      "iam.gke.io/gcp-service-account" = "${google_service_account.d2p-db-gsa.account_id}@${var.project}.iam.gserviceaccount.com"
    }
  }
}

resource "google_service_account_iam_binding" "workload_identity_binding" {
  service_account_id = google_service_account.d2p-db-gsa.name
  role               = "roles/iam.workloadIdentityUser"
  members = [
    "serviceAccount:${var.project}.svc.id.goog[${kubernetes_service_account.d2p-db-ksa.metadata.0.namespace}/${kubernetes_service_account.d2p-db-ksa.metadata.0.name}]"
  ]
}
