import {
  ConsentResponse,
  ParticipantDashboardAlert,
  ParticipantTask,
  Portal,
  PreEnrollmentResponse,
  PreregistrationResponse,
  StudyEnvironmentConsent,
  StudyEnvironmentSurvey,
  SurveyResponse
} from '@juniper/ui-core'
import { defaultApiErrorHandle } from 'util/error-utils'

export type {
  Answer,
  ConsentForm,
  ConsentResponse,
  HtmlPage,
  HtmlSection,
  LocalSiteContent,
  NavbarItem,
  NavbarItemInternal,
  NavbarItemInternalAnchor,
  NavbarItemMailingList,
  NavbarItemExternal,
  ParticipantTask,
  ParticipantTaskStatus,
  ParticipantTaskType,
  Portal,
  PortalEnvironment,
  PortalEnvironmentConfig,
  PortalStudy,
  PreEnrollmentResponse,
  PreregistrationResponse,
  SectionConfig,
  SectionType,
  SiteContent,
  Study,
  StudyEnvironment,
  StudyEnvironmentConfig,
  StudyEnvironmentConsent,
  StudyEnvironmentSurvey,
  Survey,
  SurveyResponse
} from '@juniper/ui-core'

export type ParticipantUser = {
  username: string,
  token: string
};

export type LoginResult = {
  user: ParticipantUser,
  enrollees: Enrollee[]
}

export type Enrollee = {
  id: string
  consented: boolean
  consentResponses: []
  createdAt: number
  kitRequests: []
  lastUpdatedAt: number
  participantTasks: ParticipantTask[]
  participantUserId: string
  preEnrollmentResponseId?: string
  profile: Profile
  profileId: string
  shortcode: string
  studyEnvironmentId: string
  surveyResponses: []
}

export type Profile = {
  sexAtBirth: string
}

export type KitRequest = {
  id: string,
  createdAt: number,
  kitType: KitType,
  sentToAddress?: string,
  status: string,
  sentAt?: number,
  receivedAt?: number
}

export type KitType = {
  id: string,
  name: string,
  displayName: string,
  description: string
}

export type RegistrationResponse = {
  participantUser: ParticipantUser,
  portalParticipantUser: PortalParticipantUser
}

export type ConsentWithResponses = {
  studyEnvironmentConsent: StudyEnvironmentConsent,
  consentResponses: ConsentResponse[]
}

export type SurveyWithResponse = {
  studyEnvironmentSurvey: StudyEnvironmentSurvey,
  surveyResponse?: SurveyResponse
}

export type UserResumeData = {
  currentPageNo: number
}

export type HubResponse = {
  enrollee: Enrollee,
  tasks: ParticipantTask[],
  response: object,
  profile: Profile
}

export type PortalParticipantUser = {
  profile: object
}

export type Config = {
  b2cTenantName: string,
  b2cClientId: string,
  b2cPolicyName: string,
  b2cChangePasswordPolicyName: string,
}

export type LogEvent = {
  id?: string,
  eventType: 'ERROR' | 'ACCESS' | 'EVENT' | 'STATS'
  eventName: string,
  stackTrace?: string,
  eventDetail?: string,
  studyShortcode?: string,
  portalShortcode?: string,
  environmentName?: string,
  enrolleeShortcode?: string,
  operatorId?: string,
}

let bearerToken: string | null = null
const API_ROOT = `${process.env.REACT_APP_API_ROOT}`

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

  /** get the json from the response and alert and log any errors */
  async processJsonResponse(response: Response, opts: {alertErrors: boolean} = { alertErrors: true }) {
    const obj = await response.json()
    if (response.ok) {
      return obj
    }
    if (opts.alertErrors) {
      defaultApiErrorHandle(obj)
    }
    return Promise.reject(obj)
  },

  async getConfig(): Promise<Config> {
    const response = await fetch(`/config`)
    return await this.processJsonResponse(response)
  },

  async getPortal(): Promise<Portal> {
    const { shortcodeOrHostname, envName } = currentEnvSpec
    const url = `${API_ROOT}/public/portals/v1/${shortcodeOrHostname}/env/${envName}`
    const response = await fetch(url, this.getGetInit())
    const parsedResponse: Portal = await this.processJsonResponse(response)
    updateEnvSpec(parsedResponse.shortcode)
    return parsedResponse
  },

  async getPortalEnvDashboardAlerts(portalShortcode: string, envName: string): Promise<ParticipantDashboardAlert[]> {
    const url = `${API_ROOT}/public/portals/v1/${portalShortcode}/env/${envName}/dashboard/config/alerts`
    const response = await fetch(url, this.getGetInit())
    return await this.processJsonResponse(response)
  },

  /** submit portal preregistration survey data */
  async submitPreRegResponse({ surveyStableId, surveyVersion, preRegResponse }:
                               {
                                 surveyStableId: string, surveyVersion: number,
                                 preRegResponse: Partial<PreregistrationResponse>
                               }):
    Promise<PreregistrationResponse> {
    const url = `${baseEnvUrl(true)}/preReg/${surveyStableId}/${surveyVersion}`
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
    const url = `${baseEnvUrl(true)}/preReg/${preRegId}/confirm`
    const response = await fetch(url, { headers: this.getInitHeaders() })
    if (!response.ok) {
      return Promise.reject(response)
    }
  },

  /** submit study pre-enrollment survey data */
  async submitPreEnrollResponse({ surveyStableId, surveyVersion, preEnrollResponse }:
                                  {
                                    surveyStableId: string, surveyVersion: number,
                                    preEnrollResponse: Partial<PreEnrollmentResponse>
                                  }):
    Promise<PreEnrollmentResponse> {
    const url = `${baseEnvUrl(true)}/preEnroll/${surveyStableId}/${surveyVersion}`
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
    const url = `${baseEnvUrl(true)}/preEnroll/${preRegId}/confirm`
    const response = await fetch(url, { headers: this.getInitHeaders() })
    if (!response.ok) {
      return Promise.reject(response)
    }
  },

  async register({ preRegResponseId, email, accessToken }: {
    preRegResponseId: string | null, email: string, accessToken: string
  }): Promise<LoginResult> {
    bearerToken = accessToken
    let url = `${baseEnvUrl(false)}/register`
    if (preRegResponseId) {
      url += `?preRegResponseId=${preRegResponseId}`
    }
    const response = await fetch(url, {
      method: 'POST',
      headers: this.getInitHeaders(),
      body: JSON.stringify({ email })
    })
    return await this.processJsonResponse(response)
  },

  /** submits registration data for a particular portal, from an anonymous user */
  async internalRegister({ preRegResponseId, fullData }: { preRegResponseId: string, fullData: object }):
    Promise<RegistrationResponse> {
    let url = `${baseEnvUrl(true)}/internalRegister`
    if (preRegResponseId) {
      url += `?preRegResponseId=${preRegResponseId}`
    }
    const response = await fetch(url, {
      method: 'POST',
      headers: this.getInitHeaders(),
      body: JSON.stringify(fullData)
    })
    const registrationResponse = await this.processJsonResponse(response) as RegistrationResponse
    if (registrationResponse?.participantUser?.token) {
      bearerToken = registrationResponse.participantUser.token
    }
    return registrationResponse
  },

  /** creates an enrollee for the signed-in user and study.  */
  async createEnrollee({ studyShortcode, preEnrollResponseId }:
                         { studyShortcode: string, preEnrollResponseId: string | null }):
    Promise<HubResponse> {
    let url = `${baseStudyEnvUrl(false, studyShortcode)}/enrollee`
    if (preEnrollResponseId) {
      url += `?preEnrollResponseId=${preEnrollResponseId}`
    }
    const response = await fetch(url, {
      method: 'POST',
      headers: this.getInitHeaders()
    })
    return await this.processJsonResponse(response)
  },

  async fetchConsentAndResponses({ studyShortcode, stableId, version, enrolleeShortcode }: {
    studyShortcode: string, enrolleeShortcode: string,
    stableId: string, version: number
  }): Promise<ConsentWithResponses> {
    const url = `${baseStudyEnvUrl(false, studyShortcode)}/enrollee/${enrolleeShortcode}`
      + `/consents/${stableId}/${version}`
    const response = await fetch(url, { headers: this.getInitHeaders() })
    return await this.processJsonResponse(response)
  },

  async submitConsentResponse({ studyShortcode, stableId, version, enrolleeShortcode, response }: {
    studyShortcode: string, stableId: string, version: number, response: ConsentResponse, enrolleeShortcode: string
  }): Promise<HubResponse> {
    const url = `${baseStudyEnvUrl(false, studyShortcode)}/enrollee/${enrolleeShortcode}`
      + `/consents/${stableId}/${version}`
    const result = await fetch(url, {
      method: 'POST',
      headers: this.getInitHeaders(),
      body: JSON.stringify(response)
    })
    return await this.processJsonResponse(result)
  },

  async fetchSurveyAndResponse({ studyShortcode, stableId, version, enrolleeShortcode, taskId }: {
    studyShortcode: string, enrolleeShortcode: string,
    stableId: string, version: number, taskId: string | null
  }): Promise<SurveyWithResponse> {
    let url = `${baseStudyEnvUrl(false, studyShortcode)}/enrollee/${enrolleeShortcode}`
      + `/surveys/${stableId}/${version}`
    if (taskId) {
      url = `${url}?taskId=${taskId}`
    }
    const response = await fetch(url, { headers: this.getInitHeaders() })
    return await this.processJsonResponse(response)
  },

  async updateSurveyResponse({
    studyShortcode, stableId, version, enrolleeShortcode, response, taskId,
    alertErrors=true
  }: {
    studyShortcode: string, stableId: string, version: number, response: SurveyResponse, enrolleeShortcode: string,
    taskId: string, alertErrors: boolean
  }): Promise<HubResponse> {
    let url = `${baseStudyEnvUrl(false, studyShortcode)}/enrollee/${enrolleeShortcode}`
      + `/surveys/${stableId}/${version}`
    if (taskId) {
      url = `${url}?taskId=${taskId}`
    }
    const result = await fetch(url, {
      method: 'PATCH',
      headers: this.getInitHeaders(),
      body: JSON.stringify(response)
    })
    return await this.processJsonResponse(result, { alertErrors })
  },

  async submitMailingListContact(name: string, email: string) {
    const url = `${baseEnvUrl(true)}/mailingListContact`
    const result = await fetch(url, {
      method: 'POST',
      headers: this.getInitHeaders(),
      body: JSON.stringify({ name, email })
    })
    return await this.processJsonResponse(result)
  },

  async unauthedLogin(username: string): Promise<LoginResult> {
    const url = `${baseEnvUrl(true)}/current-user/unauthed/login?${new URLSearchParams({
      username
    })}`
    const response = await fetch(url, {
      method: 'POST',
      headers: this.getInitHeaders()
    })
    const loginResult = await this.processJsonResponse(response)
    if (loginResult?.user?.token) {
      bearerToken = loginResult.user.token
    }
    return loginResult
  },

  async unauthedRefreshLogin(token: string): Promise<LoginResult> {
    bearerToken = token
    const url = `${baseEnvUrl(true)}/current-user/unauthed/refresh`
    const response = await fetch(url, {
      method: 'POST',
      headers: this.getInitHeaders()
    })
    const loginResult = await this.processJsonResponse(response, { alertErrors: false })
    if (loginResult?.user?.token) {
      bearerToken = loginResult.user.token
    }
    return loginResult
  },

  async tokenLogin(token: string): Promise<LoginResult> {
    bearerToken = token
    const url = `${baseEnvUrl(false)}/current-user/login`
    const response = await fetch(url, {
      method: 'POST',
      headers: this.getInitHeaders()
    })
    return await this.processJsonResponse(response)
  },

  async refreshLogin(token: string): Promise<LoginResult> {
    bearerToken = token
    const url = `${baseEnvUrl(false)}/current-user/refresh`
    const response = await fetch(url, {
      method: 'POST',
      headers: this.getInitHeaders()
    })
    return await this.processJsonResponse(response, { alertErrors: false })
  },

  async logout(): Promise<void> {
    const url = `${baseEnvUrl(false)}/current-user/logout`
    await fetch(url, {
      method: 'POST',
      headers: this.getInitHeaders()
    })
    bearerToken = null
  },

  setBearerToken(token: string): void {
    bearerToken = token
  },

  async log(logEvent: LogEvent): Promise<void> {
    const url = `${API_ROOT}/public/log/v1/log`
    await fetch(url, {
      method: 'POST',
      headers: this.getInitHeaders(),
      body: JSON.stringify(logEvent)
    })
  },
  /** get a url for fetching an image from the server, suitable for a src in an img tag */
  getImageUrl(cleanFileName: string, version: number) {
    return getImageUrl(cleanFileName, version)
  }
}

/** get the baseurl for endpoints that include the portal and environment */
function baseEnvUrl(isPublic: boolean) {
  const { shortcode, envName } = currentEnvSpec
  return `${API_ROOT}/${isPublic ? 'public/' : ''}portals/v1/${shortcode}/env/${envName}`
}

/** get the baseurl for endpoints that include the portal and environment and study */
function baseStudyEnvUrl(isPublic: boolean, studyShortcode: string) {
  return `${baseEnvUrl(isPublic)}/studies/${studyShortcode}`
}

/**
 * Returns a url suitable for inclusion in an <img> tag based on a image shortcode
 */
export function getImageUrl(cleanFileName: string, version: number) {
  return `${baseEnvUrl(true)}/siteImages/${version}/${cleanFileName}`
}

export type EnvSpec = {
  shortcodeOrHostname: string,
  shortcode?: string,
  envName: string
}

const ALLOWED_ENV_NAMES = [
  'sandbox', 'irb', 'live'
]
const currentEnvSpec: EnvSpec = readEnvFromHostname(window.location.hostname)

/**
 * gets the environment psec with name and shortcode.  the shortcode might not be immediately available if
 * the call to getPortal hasn't returned.
 * */
export function getEnvSpec() {
  return currentEnvSpec
}

/** updates the shortcode of the envSpec, useful for after getting the initial server response */
function updateEnvSpec(portalShortcode: string) {
  currentEnvSpec.shortcode = portalShortcode
}

/** parses shortcodeOrHostname and environment from hostname */
function readEnvFromHostname(hostname: string): EnvSpec {
  let shortcodeOrHostname
  let envName = ''
  const splitHostname = hostname.split('.')
  if (ALLOWED_ENV_NAMES.includes(splitHostname[0])) {
    envName = splitHostname[0]
    shortcodeOrHostname = splitHostname[1]
  } else {
    envName = 'live'
    shortcodeOrHostname = splitHostname[0]
  }
  return { envName, shortcodeOrHostname }
}
