# alerts can send emails to dev team
resource "google_monitoring_notification_channel" "juniper_dev_team_email" {
  display_name = "Notify Dev Team via Email"
  type         = "email"
  labels = {
    email_address = "juniper-dev-team@broadinstitute.org"
  }

  depends_on = [
    time_sleep.enable_all_services_with_timeout
  ]
}


# todo: alerts can send slack messages to dev team
