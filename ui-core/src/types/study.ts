import { ConsentForm, Survey } from './forms'

export type Study = {
  name: string
  shortcode: string
  studyEnvironments: StudyEnvironment[]
}

export type StudyEnvironment = {
  id: string
  environmentName: string
  studyEnvironmentConfig: StudyEnvironmentConfig
  preEnrollSurvey?: Survey
  preEnrollSurveyId?: string
  configuredConsents: StudyEnvironmentConsent[]
  configuredSurveys: StudyEnvironmentSurvey[]
  notificationConfigs: NotificationConfig[]
}

export type StudyEnvironmentConfig = {
  acceptingEnrollment: boolean
  initialized: boolean
  passwordProtected: boolean
  password: string
}

export type StudyEnvironmentSurvey = {
  id: string
  surveyId: string
  survey: Survey
  surveyOrder: number
  recur: boolean
  recurrenceIntervalDays: number
  allowAdminEdit: boolean
  allowParticipantStart: boolean
  allowParticipantReedit: boolean
  prepopulate: boolean
}

export type StudyEnvironmentConsent = {
  id: string
  consentForm: ConsentForm
  consentFormId: string
  consentOrder: number
  allowAdminEdit: boolean
  allowParticipantStart: boolean
  allowParticipantReedit: boolean
  prepopulate: boolean
}

export type NotificationConfig = {
  id: string
  portalEnvironmentId: string
  studyEnvironmentId: string
  active: boolean
  notificationType: string
  deliveryType: string
  rule: string
  eventType: string
  taskType: string
  taskTargetStableId: string
  afterMinutesIncomplete: number
  reminderIntervalMinutes: number
  maxNumReminders: number
  emailTemplateId: string
  emailTemplate: EmailTemplate
}

export type EmailTemplate = {
  subject: string
  body: string
}

export {}
