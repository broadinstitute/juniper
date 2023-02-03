import { DenormalizedPreEnrollResponse, DenormalizedPreRegResponse } from '../util/surveyJsUtils'

export type ParticipantUser = {
  username: string,
  token: string
};

export type LoginResult = {
  user: ParticipantUser,
  enrollees: Enrollee[]
}

export type PortalEnvironmentParams = {
  portalShortcode: string,
  environmentName: string
}

export type Portal = {
  portalEnvironments: PortalEnvironment[],
  portalStudies: PortalStudy[],
  shortcode: string
}

export type PortalEnvironment = PortalEnvironmentParams & {
  siteContent: SiteContent,
  preRegSurvey: Survey
}

export type PortalStudy = {
  study: Study,
  studyEnvironments: StudyEnvironment[]
}

export type Study = {
  name: string,
  shortcode: string,
  studyEnvironments: StudyEnvironment[]
}

export type SiteContent = {
  defaultLanguage: string,
  localizedSiteContents: LocalSiteContent[],
}

export type LocalSiteContent = {
  language: string,
  navbarItems: NavbarItem[],
  landingPage: HtmlPage,
  navLogoShortcode: string
}

export type HtmlPage = {
  title: string,
  path: string,
  sections: HtmlSection[]
}

export type NavbarItem = {
  label: string,
  externalLink: string,
  itemType: string
  htmlPage: HtmlPage
}

export type HtmlSection = {
  id: string,
  sectionType: string,
  rawContent: string | null,
  sectionConfig: string | null
}

export type SurveyJSForm = {
  stableId: string,
  version: number,
  content: string,
}

export type Survey = SurveyJSForm & {
  id: string,
  name: string,
  allowParticipantCompletion: boolean,
  allowMultipleResponses: boolean,
  allowParticipantReedit: boolean
}

export type ConsentForm = SurveyJSForm & {
  id: string,
  name: string
}

export type ResumableData = {
  currentPageNo: number,
  data: object
}

export type StudyEnvironment = {
  id: string,
  studyShortcode: string,
  preEnrollSurvey: Survey,
  siteContent: SiteContent,
  environmentName: string
}


export type StudyEnvironmentConsent = {
  id: string,
  consentFormId: string,
  consentForm: ConsentForm,
  consentOrder: number,
  allowAdminEdit: boolean,
  allowParticipantStart: boolean,
  allowParticipantReedit: boolean,
  prepopulate: boolean
}

export type ConsentResponse = {
  id: string,
  createdAt: number,
  consented: boolean,
  consentFormId: string,
  resumeData: string,
  fullData: string
}

export type PreregistrationResponse = {
  id: string
}

export type PreEnrollmentResponse = {
  id: string
}

export type Enrollee = {
  id: string,
  shortcode: string,
  studyEnvironmentId: string,
  participantTasks: ParticipantTask[]
}

export type ParticipantTask = {
  id: string,
  targetStableId: string,
  targetAssignedVersion: number,
  createdAt: number,
  targetName: string,
  taskType: string,
  blocksHub: boolean,
  taskOrder: number,
  status: string
}

export type RegistrationResponse = {
  participantUser: ParticipantUser,
  portalParticipantUser: PortalParticipantUser
}

export type ConsentWithResponses = {
  studyEnvironmentConsent: StudyEnvironmentConsent,
  consentResponses: ConsentResponse[]
}

export type PortalParticipantUser = {
  profile: object
}

export type ButtonConfig = {
  text: string,
  href: string,
  type: string, // for buttons that aren't just hrefs, a 'type' can be specified.  Currently "join" is the only type
  studyShortcode: string
}

// eslint-disable-next-line  @typescript-eslint/no-explicit-any
export type SectionConfig = { [index: string]: any }

let bearerToken: string | null = null
const API_ROOT = `${process.env.REACT_APP_API_SERVER}/${process.env.REACT_APP_API_ROOT}`

export default {
  getInitHeaders() {
    const headers: HeadersInit = {
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    }
    if (bearerToken !== null) {
      headers['Authorization'] = `Bearer ${bearerToken}`
    }
    return headers
  },

  getGetInit() {
    return {
      headers: this.getInitHeaders(),
      method: 'GET'
    }
  },

  async processJsonResponse(response: Response) {
    const obj = await response.json()
    if (response.ok) {
      return obj
    }
    return Promise.reject(response)
  },

  async getPortal(): Promise<Portal> {
    const { shortcode, envName } = getEnvSpec()
    const response = await fetch(`${API_ROOT}/portals/v1/${shortcode}/env/${envName}`, this.getGetInit())
    return await this.processJsonResponse(response)
  },

  /** submit portal preregistration survey data */
  async submitPreRegResponse({ surveyStableId, surveyVersion, preRegResponse }:
                               {
                                 surveyStableId: string, surveyVersion: number,
                                 preRegResponse: DenormalizedPreRegResponse
                               }):
    Promise<PreregistrationResponse> {
    const url = `${baseEnvUrl()}/preReg/${surveyStableId}/${surveyVersion}`
    const response = await fetch(url, {
      method: 'POST',
      headers: this.getInitHeaders(),
      body: JSON.stringify(preRegResponse)
    })
    return await this.processJsonResponse(response)
  },

  /**
   * confirms that a client-side saved preregistration id is still valid.  For cases where the user refreshes the
   * page while on registration
   */
  async confirmPreRegResponse(preRegId: string):
    Promise<void> {
    const url = `${baseEnvUrl()}/preReg/${preRegId}/confirm`
    const response = await fetch(url, { headers: this.getInitHeaders() })
    if (!response.ok) {
      return Promise.reject(response)
    }
  },

  /** submit study pre-enrollment survey data */
  async submitPreEnrollResponse({ surveyStableId, surveyVersion, preEnrollResponse }:
                                  {
                                    surveyStableId: string, surveyVersion: number,
                                    preEnrollResponse: DenormalizedPreEnrollResponse
                                  }):
    Promise<PreEnrollmentResponse> {
    const url = `${baseEnvUrl()}/preEnroll/${surveyStableId}/${surveyVersion}`
    const response = await fetch(url, {
      method: 'POST',
      headers: this.getInitHeaders(),
      body: JSON.stringify(preEnrollResponse)
    })
    return await this.processJsonResponse(response)
  },

  /**
   * confirms that a client-side saved preregistration id is still valid.  For cases where the user refreshes the
   * page while on registration
   */
  async confirmPreEnrollResponse(preRegId: string):
    Promise<void> {
    const url = `${baseEnvUrl()}/preEnroll/${preRegId}/confirm`
    const response = await fetch(url, { headers: this.getInitHeaders() })
    if (!response.ok) {
      return Promise.reject(response)
    }
  },

  /** submits registration data for a particular portal, from an anonymous user */
  async register({ preRegResponseId, fullData }: { preRegResponseId: string, fullData: object }):
    Promise<RegistrationResponse> {
    let url = `${baseEnvUrl()}/register`
    if (preRegResponseId) {
      url += `?preRegResponseId=${preRegResponseId}`
    }
    const response = await fetch(url, {
      method: 'POST',
      headers: this.getInitHeaders(),
      body: JSON.stringify(fullData)
    })
    return await this.processJsonResponse(response)
  },

  /** creates an enrollee for the signed-in user and study.  */
  async createEnrollee({ studyShortcode, preEnrollResponseId }:
                         { studyShortcode: string, preEnrollResponseId: string | null }):
    Promise<Enrollee> {
    let url = `${baseEnvUrl()}/studies/${studyShortcode}/enrollee`
    if (preEnrollResponseId) {
      url += `?preEnrollResponseId=${preEnrollResponseId}`
    }
    const response = await fetch(url, {
      method: 'POST',
      headers: this.getInitHeaders()
    })
    return await this.processJsonResponse(response)
  },

  async fetchConsentAndResponses({ studyShortcode, stableId, version, taskId }: {
    studyShortcode: string,
    stableId: string, version: number, taskId: string | null
  }): Promise<ConsentWithResponses> {
    let url = `${baseStudyEnvUrl(studyShortcode)}/consents/${stableId}/${version}`
    if (taskId) {
      url = `${url}?taskId=${taskId}`
    }
    const response = await fetch(url, { headers: this.getInitHeaders() })
    return await this.processJsonResponse(response)
  },

  async unauthedLogin(username: string): Promise<LoginResult> {
    const url = `${baseEnvUrl()}/current-user/unauthed-login?${new URLSearchParams({
      username
    })}`
    const response = await fetch(url, {
      method: 'POST',
      headers: this.getInitHeaders()
    })
    return await this.processJsonResponse(response)
  },

  async refreshLogin(token: string): Promise<LoginResult> {
    this.setBearerToken(token)

    const url = `${baseEnvUrl()}/current-user/refresh`
    const response = await fetch(url, {
      method: 'POST',
      headers: this.getInitHeaders()
    })
    return await this.processJsonResponse(response)
  },

  async logout(): Promise<void> {
    const url = `${baseEnvUrl()}/current-user/logout`
    await fetch(url, {
      method: 'POST',
      headers: this.getInitHeaders()
    })
  },

  setBearerToken(token: string | null) {
    bearerToken = token
  }
}

/** get the baseurl for endpoints that include the portal and environment */
function baseEnvUrl() {
  const { shortcode, envName } = getEnvSpec()
  return `${API_ROOT}/portals/v1/${shortcode}/env/${envName}`
}

/** get the baseurl for endpoints that include the portal and environment and study */
function baseStudyEnvUrl(studyShortcode: string) {
  const { shortcode, envName } = getEnvSpec()
  return `${API_ROOT}/portals/v1/${shortcode}/env/${envName}/studies/${studyShortcode}`
}

/**
 * Returns a url suitable for inclusion in an <img> tag based on a image shortcode
 */
export function getImageUrl(imageShortcode: string) {
  return `${baseEnvUrl()}/siteImages/${imageShortcode}`
}

export type EnvSpec = {
  shortcode: string,
  envName: string
}

/** gets the current environment params */
export function getEnvSpec(): EnvSpec {
  return readEnvFromHostname(window.location.hostname)
}

/** parses shortcode and environment from hostname */
function readEnvFromHostname(hostname: string): EnvSpec {
  let shortname
  let envName = ''
  const splitHostname = hostname.split('.')
  if (Object.keys(ALLOWED_ENV_NAMES).includes(splitHostname[0])) {
    envName = ALLOWED_ENV_NAMES[splitHostname[0]]
    shortname = splitHostname[1]
  } else {
    envName = 'LIVE'
    shortname = splitHostname[0]
  }
  return { envName, shortcode: shortname }
}

const ALLOWED_ENV_NAMES: Record<string, string> = {
  'sandbox': 'SANDBOX',
  'irb': 'IRB',
  'live': 'LIVE'
}

