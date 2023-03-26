import {ConsentResponseDto, PreEnrollResponseDto, PreRegResponseDto, SurveyResponseDto} from '../util/surveyJsUtils'

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

export type SiteContent = {
  defaultLanguage: string,
  localizedSiteContents: LocalSiteContent[],
}

export type LocalSiteContent = {
  language: string,
  navbarItems: NavbarItem[],
  landingPage: HtmlPage,
  navLogoCleanFileName: string,
  navLogoVersion: number,
  footerSection?: HtmlSection
  primaryBrandColor?: string
}

export type HtmlPage = {
  title: string,
  path: string,
  sections: HtmlSection[]
}

export type NavbarItem = {
  label: string,
  externalLink?: string,
  anchorLinkPath?: string,
  itemType: string
  htmlPage?: HtmlPage
}

export type NavbarItemInternal = NavbarItem & {
  htmlPage: HtmlPage
}

export type NavbarItemInternalAnchor = NavbarItem & {
  anchorLinkPath: string
}

/** type predicate for handling internal links */
export function isInternalLink(navItem: NavbarItem): navItem is NavbarItemInternal {
  return navItem.itemType === 'INTERNAL'
}

/** type predicate for handling internal anchor links */
export function isInternalAnchorLink(navItem: NavbarItem): navItem is NavbarItemInternalAnchor {
  return navItem.itemType === 'INTERNAL_ANCHOR'
}

export type SectionType =
  | 'BANNER_IMAGE'
  | 'FAQ'
  | 'HERO_CENTERED'
  | 'HERO_WITH_IMAGE'
  | 'NAV_AND_LINK_SECTIONS_FOOTER'
  | 'PARTICIPATION_DETAIL'
  | 'PHOTO_BLURB_GRID'
  | 'RAW_HTML'
  | 'SOCIAL_MEDIA'
  | 'STEP_OVERVIEW'

export type HtmlSection = {
  id: string,
  sectionType: SectionType,
  anchorRef?: string,
  rawContent?: string | null,
  sectionConfig?: string | null
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
  participantTasks: ParticipantTask[],
  consented: boolean
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
  surveyResponse: SurveyResponse
}

export type ResponseSnapshot = {
  createdAt: string,
  resumeData: string,
  fullData: string
}

export type SurveyResponse = {
  createdAt: number, // this is a java instant, so number of seconds since epoch start
  lastUpdatedAt: string,
  surveyId: string,
  surveyStableId: string,
  surveyVersion: string,
  lastSnapshot: ResponseSnapshot
}

export type HubResponse = {
  enrollee: Enrollee,
  tasks: ParticipantTask[],
  response: object
}

export type PortalParticipantUser = {
  profile: object
}

export type Config = {
  b2cTenantName: string,
  b2cClientId: string
}

export type SectionConfig = Record<string, unknown>

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
    const response = await fetch(baseEnvUrl(true), this.getGetInit())
    return await this.processJsonResponse(response)
  },

  /** submit portal preregistration survey data */
  async submitPreRegResponse({surveyStableId, surveyVersion, preRegResponse}:
                               {
                                 surveyStableId: string, surveyVersion: number,
                                 preRegResponse: PreRegResponseDto
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
    const response = await fetch(url, {headers: this.getInitHeaders()})
    if (!response.ok) {
      return Promise.reject(response)
    }
  },

  /** submit study pre-enrollment survey data */
  async submitPreEnrollResponse({surveyStableId, surveyVersion, preEnrollResponse}:
                                  {
                                    surveyStableId: string, surveyVersion: number,
                                    preEnrollResponse: PreEnrollResponseDto
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
    const response = await fetch(url, {headers: this.getInitHeaders()})
    if (!response.ok) {
      return Promise.reject(response)
    }
  },

  async register({preRegResponseId, email, accessToken}: {
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
      body: JSON.stringify({email})
    })
    return await this.processJsonResponse(response)
  },

  /** submits registration data for a particular portal, from an anonymous user */
  async internalRegister({preRegResponseId, fullData}: { preRegResponseId: string, fullData: object }):
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
  async createEnrollee({studyShortcode, preEnrollResponseId}:
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

  async fetchConsentAndResponses({studyShortcode, stableId, version, enrolleeShortcode, taskId}: {
    studyShortcode: string, enrolleeShortcode: string,
    stableId: string, version: number, taskId: string | null
  }): Promise<ConsentWithResponses> {
    let url = `${baseStudyEnvUrl(false, studyShortcode)}/enrollee/${enrolleeShortcode}`
      + `/consents/${stableId}/${version}`
    if (taskId) {
      url = `${url}?taskId=${taskId}`
    }
    const response = await fetch(url, {headers: this.getInitHeaders()})
    return await this.processJsonResponse(response)
  },

  async submitConsentResponse({studyShortcode, stableId, version, enrolleeShortcode, response, taskId}: {
    studyShortcode: string, stableId: string, version: number, response: ConsentResponseDto, enrolleeShortcode: string,
    taskId: string
  }): Promise<HubResponse> {
    let url = `${baseStudyEnvUrl(false, studyShortcode)}/enrollee/${enrolleeShortcode}`
      + `/consents/${stableId}/${version}`
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

  async fetchSurveyAndResponse({studyShortcode, stableId, version, enrolleeShortcode, taskId}: {
    studyShortcode: string, enrolleeShortcode: string,
    stableId: string, version: number, taskId: string | null
  }): Promise<SurveyWithResponse> {
    let url = `${baseStudyEnvUrl(false, studyShortcode)}/enrollee/${enrolleeShortcode}`
      + `/surveys/${stableId}/${version}`
    if (taskId) {
      url = `${url}?taskId=${taskId}`
    }
    const response = await fetch(url, {headers: this.getInitHeaders()})
    return await this.processJsonResponse(response)
  },

  async submitSurveyResponse({studyShortcode, stableId, version, enrolleeShortcode, response, taskId}: {
    studyShortcode: string, stableId: string, version: number, response: SurveyResponseDto, enrolleeShortcode: string,
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
    let url = `${baseEnvUrl(true)}/mailingListContact`
    const result = await fetch(url, {
      method: 'POST',
      headers: this.getInitHeaders(),
      body: JSON.stringify({name, email})
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
  }
}

/** get the baseurl for endpoints that include the portal and environment */
function baseEnvUrl(isPublic: boolean) {
  const {shortcode, envName} = getEnvSpec()
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
  return {envName, shortcode: shortname}
}

const ALLOWED_ENV_NAMES: Record<string, string> = {
  'sandbox': 'SANDBOX',
  'irb': 'IRB',
  'live': 'LIVE'
}
