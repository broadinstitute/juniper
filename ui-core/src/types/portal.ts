import { Survey } from './forms'
import { SiteContent } from './landingPageConfig'
import { Study } from './study'

export type Portal = {
  id: string
  name: string
  shortcode: string
  portalEnvironments: PortalEnvironment[]
  portalStudies: PortalStudy[]
}

export type PortalEnvironmentLanguage = {
  languageCode: string
  languageName: string
}

export type PortalStudy = {
  study: Study
}

export type PortalEnvironment = {
  environmentName: string
  portalEnvironmentConfig: PortalEnvironmentConfig
  supportedLanguages: PortalEnvironmentLanguage[]
  siteContent?: SiteContent
  preRegSurvey?: Survey
  preRegSurveyId?: string
}

export type PortalEnvironmentConfig = {
  acceptingRegistration: boolean
  initialized: boolean
  password: string
  passwordProtected: boolean
  participantHostname?: string
  emailSourceAddress?: string
}

export {}
