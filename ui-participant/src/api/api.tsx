import { SiteContent } from '@juniper/ui-core'

export type {
  SiteContent,
  LocalSiteContent,
  HtmlPage,
  HtmlSection,
  SectionConfig,
  SectionType,
  NavbarItem,
  NavbarItemInternal,
  NavbarItemInternalAnchor
} from '@juniper/ui-core'
export {
  isInternalLink,
  isInternalAnchorLink
} from '@juniper/ui-core'

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
  name: string
  portalEnvironments: PortalEnvironment[],
  portalStudies: PortalStudy[],
  shortcode: string
}

export type PortalEnvironment = PortalEnvironmentParams & {
  siteContent: SiteContent,
  preRegSurvey: Survey,
  portalEnvironmentConfig: PortalEnvironmentConfig
}

export type PortalEnvironmentConfig = {
  password: string,
  passwordProtected: boolean,
  allowRegistration: boolean
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

export type SurveyJSForm = {
  stableId: string,
  version: number,
  content: string
}

export type Survey = SurveyJSForm & {
  id: string,
  name: string,
  footer?: string
}

export type ConsentForm = SurveyJSForm & {
  id: string,
  name: string
}

export type SurveyJsResumeData = {
  currentPageNo: number,
  data: object
}

export type StudyEnvironment = {
  id: string,
  studyEnvironmentConfig: StudyEnvironmentConfig,
  studyShortcode: string,
  preEnrollSurvey: Survey,
  environmentName: string
}

export type StudyEnvironmentConfig = {
  acceptingEnrollment: boolean
  initialized: boolean
  passwordProtected: boolean
  password: string
}

export type StudyEnvironmentSurvey = {
  id: string,
  surveyId: string,
  survey: Survey,
  surveyOrder: number,
  allowAdminEdit: boolean,
  allowParticipantStart: boolean,
  allowParticipantReedit: boolean,
  prepopulate: boolean
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

export type FormResponse = {
  id?: string,
  createdAt?: number,
  enrolleeId?: string,
  resumeData: string,
  creatingParticipantUserId?: string,
}

export type ConsentResponse = FormResponse & {
  consentFormId: string,
  consented: boolean,
  fullData: string
}

export type SurveyResponse = FormResponse & {
  surveyId: string,
  complete: boolean,
  answers: Answer[]
}

export type PreregistrationResponse = FormResponse & {
  qualified: false,
  surveyId: string,
  answers: Answer[]
}

export type PreEnrollmentResponse = FormResponse & {
  qualified: false,
  surveyId: string,
  studyEnvironmentId: string,
  answers: Answer[]
}

export type Enrollee = {
  id: string,
  participantUserId: string,
  shortcode: string,
  studyEnvironmentId: string,
  participantTasks: ParticipantTask[],
  consented: boolean,
  profile: Profile
}

export type Profile = {
  sexAtBirth: string
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

export type SurveyWithResponse = {
  studyEnvironmentSurvey: StudyEnvironmentSurvey,
  surveyResponse?: SurveyResponse
}

export type UserResumeData = {
  currentPageNo: number
}

export type Answer = {
  stringValue?: string,
  numberValue?: number,
  booleanValue?: boolean,
  objectValue?: string,
  questionStableId: string,
  otherDescription?: string
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

  async processJsonResponse(response: Response) {
    const obj = await response.json()
    if (response.ok) {
      return obj
    }
    return Promise.reject(response)
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

  /** submit portal preregistration survey data */
  async submitPreRegResponse({ surveyStableId, surveyVersion, preRegResponse }:
                               {
                                 surveyStableId: string, surveyVersion: number,
                                 preRegResponse: PreregistrationResponse
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
                                    preEnrollResponse: PreEnrollmentResponse
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

  async submitSurveyResponse({ studyShortcode, stableId, version, enrolleeShortcode, response, taskId }: {
    studyShortcode: string, stableId: string, version: number, response: SurveyResponse, enrolleeShortcode: string,
    taskId: string
  }): Promise<HubResponse> {
    let url = `${baseStudyEnvUrl(false, studyShortcode)}/enrollee/${enrolleeShortcode}`
      + `/surveys/${stableId}/${version}`
    if (taskId) {
      url = `${url}?taskId=${taskId}`
    }
    const result = await fetch(url, {
      method: 'POST',
      headers: this.getInitHeaders(),
      body: JSON.stringify(response)
    })
    return await this.processJsonResponse(result)
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
    const loginResult = await this.processJsonResponse(response)
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
    return await this.processJsonResponse(response)
  },

  async logout(): Promise<void> {
    const url = `${baseEnvUrl(false)}/current-user/logout`
    await fetch(url, {
      method: 'POST',
      headers: this.getInitHeaders()
    })
    bearerToken = null
  },

  async log(logEvent: LogEvent): Promise<void> {
    const url = `${API_ROOT}/public/log/v1/log`
    await fetch(url, {
      method: 'POST',
      headers: this.getInitHeaders(),
      body: JSON.stringify(logEvent)
    })
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
