import { Survey } from './forms'
import { ParticipantTaskStatus } from './task'
import { KitType } from 'src/types/kits'

export type Study = {
  name: string
  shortcode: string
  studyEnvironments: StudyEnvironment[]
}

export type EnvironmentName = 'sandbox' | 'irb' | 'live'
export const ENVIRONMENT_NAMES: EnvironmentName[] =  ['sandbox', 'irb', 'live']

export type StudyEnvParams = {
  studyShortcode: string
  envName: EnvironmentName
  portalShortcode: string
}

export type StudyEnvironment = {
  id: string
  environmentName: EnvironmentName
  studyEnvironmentConfig: StudyEnvironmentConfig
  preEnrollSurvey?: Survey
  preEnrollSurveyId?: string
  configuredSurveys: StudyEnvironmentSurvey[]
  triggers: Trigger[]
  kitTypes: KitType[]
}

export type StudyEnvironmentConfig = {
  acceptingEnrollment: boolean
  acceptingProxyEnrollment: boolean
  enableFamilyLinkage: boolean
  initialized: boolean
  passwordProtected: boolean
  password: string
  useStubDsm: boolean
  useDevDsmRealm: boolean
  enableInPersonKits: boolean
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

export type TriggerType = 'EVENT' | 'TASK_REMINDER' | 'AD_HOC'
export type TriggerDeliveryType = 'EMAIL'
export type TriggerActionType = 'NOTIFICATION' | 'ADMIN_NOTIFICATION' | 'TASK_STATUS_CHANGE'
export type TriggerScope = 'PORTAL' | 'STUDY'

export type Trigger = {
  id: string
  portalEnvironmentId: string
  studyEnvironmentId: string
  active: boolean
  triggerType: TriggerType
  deliveryType: TriggerDeliveryType
  actionType: TriggerActionType
  rule?: string
  eventType: string
  taskType?: string
  actionScope: TriggerScope
  updateTaskTargetStableId?: string
  statusToUpdateTo?: ParticipantTaskStatus
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
