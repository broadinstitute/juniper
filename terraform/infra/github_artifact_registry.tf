# this file allows github to create artifacts in the artifact registry


# terraform translation of https://gist.github.com/palewire/12c4b2b974ef735d22da7493cf7f4d37

# 0. create service account
resource "google_service_account" "github_actions" {
  account_id = "github-actions"
  display_name = "github-actions"
  project = var.project
}

# 1. create workload identity pool
resource "google_iam_workload_identity_pool" "github_actions_pool" {
  workload_identity_pool_id = "github-actions"
  provider = google-beta
  project = var.project
  display_name = "github-wip"
}

# 2. create workload identity provider
resource "google_iam_workload_identity_pool_provider" "github_actions_pool_provider" {
  workload_identity_pool_id = google_iam_workload_identity_pool.github_actions_pool.workload_identity_pool_id
  workload_identity_pool_provider_id = "github-actions-provider"
  display_name = "github-actions-provider"
  attribute_mapping  = {
    "google.subject"             = "assertion.sub"
    "attribute.actor"            = "assertion.actor"
    "attribute.aud"              = "assertion.aud"
    "attribute.repository"       = "assertion.repository"
    "attribute.repository_owner" = "assertion.repository_owner"
  }

  # NOTE: this is what restricts external access, this ids are from github
  attribute_condition = "assertion.repository_owner_id == '393552' && assertion.repository_id == '566938309'"
  oidc {
    allowed_audiences = []
    issuer_uri = "https://token.actions.githubusercontent.com"
  }

}

# 3. create iam policy binding to connect the service account to the workload identity pool
resource "google_service_account_iam_binding" "github_sa_iam" {
  service_account_id = google_service_account.github_actions.name
  role = "roles/iam.workloadIdentityUser"
  members = [
    "principalSet://iam.googleapis.com/${google_iam_workload_identity_pool.github_actions_pool.name}/attribute.repository/broadinstitute/juniper"
  ]
}



# 4. create iam policy binding for access to GCR
resource "google_artifact_registry_repository_iam_binding" "github_artifact_registry_iam" {
    repository = google_artifact_registry_repository.juniper_repo.name
    location = var.region
    project = var.project
    role = "roles/artifactregistry.reader"
    members = [
        "serviceAccount:${google_service_account.github_actions.email}"
    ]
}
