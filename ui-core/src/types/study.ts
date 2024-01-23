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
  triggers: Trigger[]
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
  studyEnvironmentId?: string
}

export type StudyEnvironmentConsent = {
  id: string
  consentForm: ConsentForm
  consentFormId: string
  studyEnvironmentId: string
  consentOrder: number
  allowAdminEdit: boolean
  allowParticipantStart: boolean
  allowParticipantReedit: boolean
  prepopulate: boolean
}

export type Trigger = {
  id: string
  portalEnvironmentId: string
  studyEnvironmentId: string
  active: boolean
  triggerType: string
  deliveryType: string
  rule?: string
  eventType: string
  taskType?: string
  taskTargetStableId?: string
  afterMinutesIncomplete: number
  reminderIntervalMinutes: number
  maxNumReminders: number
  emailTemplateId: string
  emailTemplate: EmailTemplate
}

export type EmailTemplate = {
  id?: string  // id may not be present if the template is newly created client-side
  subject: string
  body: string
  name: string
  stableId: string
  version: number
}

export {}
