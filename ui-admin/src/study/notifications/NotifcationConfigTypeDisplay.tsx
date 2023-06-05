import React from 'react'
import { NotificationConfig } from 'api/api'

export const deliveryTypeDisplayMap: Record<string, string> = {
  EMAIL: 'email'
}

export const eventTypeDisplayMap: Record<string, string> = {
  PORTAL_REGISTRATION: 'Portal registration',
  SURVEY_RESPONSE: 'Survey response',
  STUDY_ENROLLMENT: 'Study enrollment',
  STUDY_CONSENT: 'Consent form submission'
}

/** shows a summary of the notification config */
export default function NotificationConfigTypeDisplay({ config }: {config?: NotificationConfig}) {
  if (!config) {
    return <></>
  }
  if (config.notificationType === 'EVENT') {
    return <span>{eventTypeDisplayMap[config.eventType]}</span>
  } else if (config.notificationType === 'TASK_REMINDER') {
    return <span>
      Reminder: {config.taskType} {config.taskTargetStableId}
    </span>
  } else if (config.notificationType === 'AD_HOC') {
    return <span>Ad-Hoc</span>
  }
  return <span>{config.taskType}</span>
}
