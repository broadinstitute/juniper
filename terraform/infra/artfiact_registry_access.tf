# Grants cross-project access to the artifact registry
# for the Juniper cluster & cloudbuild service accounts

resource "google_artifact_registry_repository_iam_binding" "cluster-artifact-registry-reader" {
  role   = "roles/artifactregistry.reader"
  repository = "juniper"
  members = [
    "serviceAccount:juniper-cluster@broad-juniper-dev.iam.gserviceaccount.com",
    "serviceAccount:juniper-cluster@broad-juniper-prod.iam.gserviceaccount.com",
    "serviceAccount:juniper-cloudbuild-sa@broad-juniper-dev.iam.gserviceaccount.com",
    "serviceAccount:juniper-cloudbuild-sa@broad-juniper-prod.iam.gserviceaccount.com",
    "serviceAccount:service-663573365422@serverless-robot-prod.iam.gserviceaccount.com", # cloud run service account
  ]
}
