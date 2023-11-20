import { AlertLevel } from 'src/participant/dashboard/Alert'

export type MessageTrigger = 'NO_ACTIVITIES_REMAIN' |
  'WELCOME' |
  'STUDY_ALREADY_ENROLLED'

export type ParticipantDashboardAlert = {
  id?: string
  title: string
  detail?: string
  type: AlertLevel
  trigger: MessageTrigger
}

export const alertDefaults: Record<MessageTrigger, ParticipantDashboardAlert> = {
  'NO_ACTIVITIES_REMAIN': {
    title: 'All activities complete',
    detail: 'You have completed all activities for this study. We will notify you ' +
      'as soon as new activities are available. Thank you for your participation!',
    type: 'primary',
    trigger: 'NO_ACTIVITIES_REMAIN'
  },
  'WELCOME': {
    title: 'Welcome to the study.',
    detail: 'Please read and sign the consent form below to continue.',
    type: 'info',
    trigger: 'WELCOME'
  },
  'STUDY_ALREADY_ENROLLED': {
    title: 'You are already enrolled in this study.',
    type: 'info',
    trigger: 'STUDY_ALREADY_ENROLLED'
  }
}
