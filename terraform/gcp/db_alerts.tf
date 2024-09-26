locals {
  utilization_conditions = [
    {
      metric_type = "cloudsql.googleapis.com/database/cpu/utilization"
      display_name = "Cloud SQL Database - CPU utilization"
    },
    {
      metric_type = "cloudsql.googleapis.com/database/memory/utilization"
      display_name = "Cloud SQL Database - Memory utilization"
    }
  ]
}

# alert via email when CPU or memory utilization exceeds 75% for over 1 minute
resource "google_monitoring_alert_policy" "cpu_memory_alert_policy" {
  display_name = "Database usage alerts"
  documentation {
    content = "The $${metric.display_name} of the $${resource.type} $${resource.label.instance_id} in $${resource.project} has exceeded 75% for over 1 minute."
  }
  combiner     = "OR"
  dynamic "conditions" {
    for_each = local.utilization_conditions
    content {
      display_name = conditions.value.display_name
      condition_threshold {
        comparison = "COMPARISON_GT"
        duration = "60s"
        aggregations {
          alignment_period   = "60s"
          cross_series_reducer = "REDUCE_NONE"
          per_series_aligner = "ALIGN_MEAN"
        }
        filter = "resource.type = \"cloudsql_database\" AND metric.type = \"${conditions.value.metric_type}\""
        threshold_value = "0.75"
        trigger {
          percent = "100"
        }
      }
    }
  }

  alert_strategy {
    notification_channel_strategy {
      renotify_interval = "1800s"
      notification_channel_names = [google_monitoring_notification_channel.juniper_dev_team_email.name]
    }
  }

  notification_channels = [google_monitoring_notification_channel.juniper_dev_team_email.name]

  user_labels = {
    severity = "warning"
  }
}

# alert via email when read/write ops/sec exceeds 1000
# TODO: read/write ops/sec alerts (>1000)

# alert via email when disk utilization exceeds 80%
# TODO: disk utilization alert (>80%)
