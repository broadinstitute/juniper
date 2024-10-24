
# Create GSA and assign roles

resource "google_service_account" "junper-app-gsa" {
  account_id = "juniper-app-gsa"
}

resource "google_sql_user" "junper_app_service_account_user" {
  # Note: for PostgreSQL only, Google Cloud requires that you omit the
  # ".gserviceaccount.com" suffix
  # from the service account email due to length limits on database usernames.
  name     = trimsuffix(google_service_account.junper-app-gsa.email, ".gserviceaccount.com")
  instance = google_sql_database_instance.d2p.name
  type     = "CLOUD_IAM_SERVICE_ACCOUNT"
}

resource "google_project_iam_binding" "access-postgres" {
  project = var.project
  role    = "roles/cloudsql.client"
  members = [
    "serviceAccount:${google_service_account.junper-app-gsa.email}"
  ]
}

resource "google_project_iam_binding" "access-secrets" {
  project = var.project
  role    = "roles/secretmanager.secretAccessor"
  members = [
    "serviceAccount:${google_service_account.junper-app-gsa.email}"
  ]
}

resource "google_service_account_iam_binding" "workload_identity_binding" {
  service_account_id = google_service_account.junper-app-gsa.name
  role               = "roles/iam.workloadIdentityUser"
  members = [
    "serviceAccount:${var.project}.svc.id.goog[${var.k8s_namespace}/juniper-app-ksa]"
  ]
}

# actual ksa user is created in the k8s module
