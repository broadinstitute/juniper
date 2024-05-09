import { Survey } from './forms'

export type Study = {
  name: string
  shortcode: string
  studyEnvironments: StudyEnvironment[]
}

export type EnvironmentName = 'sandbox' | 'irb' | 'live'
export const ENVIRONMENT_NAMES: EnvironmentName[] =  ['sandbox', 'irb', 'live']

export type StudyEnvironment = {
  id: string
  environmentName: EnvironmentName
  studyEnvironmentConfig: StudyEnvironmentConfig
  preEnrollSurvey?: Survey
  preEnrollSurveyId?: string
  configuredSurveys: StudyEnvironmentSurvey[]
  triggers: Trigger[]
}

export type StudyEnvironmentConfig = {
  acceptingEnrollment: boolean
  acceptingProxyEnrollment: boolean
  initialized: boolean
  passwordProtected: boolean
  password: string
  useStubDsm: boolean
  useDevDsmRealm: boolean
}

export type StudyEnvironmentSurvey = {
  id: string
  surveyId: string
  survey: Survey
  surveyOrder: number
  studyEnvironmentId?: string
  lastUpdatedAt?: number
  createdAt?: number
}

export type StudyEnvironmentSurveyNamed = StudyEnvironmentSurvey & {
  envName: EnvironmentName
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
  stableId: string
  name: string
  version: number
  localizedEmailTemplates: LocalizedEmailTemplate[]
}

export type LocalizedEmailTemplate = {
  id?: string  // id may not be present if the template is newly created client-side
  subject: string
  body: string
  language: string
}

export {}
