
# adapted for our use from https://github.com/GoogleCloudPlatform/docker-clamav-malware-scanner/tree/main/terraform


# Copyright 2024 Google LLC
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.


## Lookup the hash of the latest image
##
data "google_artifact_registry_docker_image" "scanner-service-image" {
  location      = var.artifact_registry_location
  repository_id = var.artifact_registry
  project       = var.artifact_registry_project
  image_name    = "${var.malware_scanner_image_name}:latest"
}

## Deploy the Cloud Run Service
#
resource "google_cloud_run_v2_service" "malware_scanner" {
  name     = "juniper-malware-scanner"
  location = var.region
  ingress  = "INGRESS_TRAFFIC_INTERNAL_ONLY"

  template {
    scaling {
      max_instance_count = 5
      min_instance_count = 1
    }
    service_account                  = google_service_account.malware_scanner_sa.email
    timeout                          = "300s"
    max_instance_request_concurrency = 20

    containers {
      image = data.google_artifact_registry_docker_image.scanner-service-image.self_link
      resources {
        limits = {
          cpu    = "1"
          memory = "4Gi"
        }
        cpu_idle          = false # CPU is still allocated outside of requests
        startup_cpu_boost = true
      }
      env {
        name  = "CONFIG_JSON"
        value = jsonencode({
          "buckets": [
            {
              "unscanned": google_storage_bucket.unscanned_participant_documents.name,
              "clean": google_storage_bucket.clean_participant_documents.name,
              "quarantined": google_storage_bucket.quarantined_participant_documents.name,
            }
          ],
          "ClamCvdMirrorBucket": google_storage_bucket.cvd_mirror_bucket.name,
          "fileExclusionPatterns": [["\\\\.tmp$","i"]],
          "ignoreZeroLengthFiles": true,
          "quarantine": {
            "encryptedFiles": true,
            "fileExtensionAllowList": [],
            "fileExtensionDenyList": []
          }
        })
      }

      startup_probe {
        # Allow 90 secs before we start probing
        # Then allow up to 15*10 = 150s before we give up
        # (total possible startup time = 240s)
        initial_delay_seconds = 90
        failure_threshold     = 15
        period_seconds        = 10
        timeout_seconds       = 1

        http_get {
          path = "/ready"
        }
      }

      liveness_probe {
        # Poll every 30 secs, allowing for 3 failures
        #
        period_seconds    = 30
        failure_threshold = 3
        timeout_seconds   = 30

        http_get {
          path = "/ready"
        }
      }
    }
  }
  traffic {
    type    = "TRAFFIC_TARGET_ALLOCATION_TYPE_LATEST"
    percent = 100
  }

  depends_on = [
    google_storage_bucket.unscanned_participant_documents,
    google_storage_bucket.clean_participant_documents,
    google_storage_bucket.quarantined_participant_documents,
    google_storage_bucket.cvd_mirror_bucket,
    time_sleep.enable_all_services_with_timeout
  ]
}


## Create EventArc Triggers on unscanned bucket(s)
#
resource "google_eventarc_trigger" "gcs-object-written" {
  name     = "gcs-trigger-${google_storage_bucket.unscanned_participant_documents.name}"
  location = var.region
  matching_criteria {
    attribute = "type"
    value     = "google.cloud.storage.object.v1.finalized"
  }
  matching_criteria {
    attribute = "bucket"
    value     = google_storage_bucket.unscanned_participant_documents.name
  }
  destination {
    cloud_run_service {
      service = google_cloud_run_v2_service.malware_scanner.name
      region  = google_cloud_run_v2_service.malware_scanner.location
    }
  }
  service_account = google_service_account.malware_scanner_sa.email

  depends_on = [
    time_sleep.enable_all_services_with_timeout
  ]
}

## Update pubsub subscriptions to increase deadlines
#
resource "null_resource" "update-subscription-ack-deadline" {
  provisioner "local-exec" {
    command = "gcloud pubsub subscriptions update \"${google_eventarc_trigger.gcs-object-written.transport[0].pubsub[0].subscription}\" --ack-deadline=300"
  }
}

## Deploy scheduled task to refresh the CVD Mirror

# To avoid having too many clients use the same time slot,
# ClamAV requires that updates are scheduled at a random minute between 3
# and 57 avoiding multiples of 10.
resource "random_integer" "cvd_mirror_update_schedule_minutes" {
  min = 3
  max = 57
}

locals {
  # Avoid multiples of 10 by subtracting 3.
  cvd_mirror_update_schedule_minutes = (
    random_integer.cvd_mirror_update_schedule_minutes.result % 10 == 0
    ? random_integer.cvd_mirror_update_schedule_minutes.result - 3
    : random_integer.cvd_mirror_update_schedule_minutes.result
  )
}

resource "google_cloud_scheduler_job" "cvd_mirror_update" {
  name             = "juniper-malware-scanner-cvd-mirror-update"
  schedule         = "${local.cvd_mirror_update_schedule_minutes} */2 * * *"
  attempt_deadline = "320s"
  region           = var.region
  http_target {
    http_method = "POST"
    uri         = google_cloud_run_v2_service.malware_scanner.uri
    body        = base64encode("{\"kind\":\"schedule#cvd_update\"}")
    headers = {
      "Content-Type" = "application/json"
    }
    oidc_token {
      service_account_email = google_service_account.malware_scanner_sa.email
    }
  }

  depends_on = [
    time_sleep.enable_all_services_with_timeout
  ]
}
