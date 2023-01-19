import {DenormalizedPreRegResponse} from '../util/surveyJsUtils'

export type ParticipantUser = {
  username: string,
  token: string
};

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
  studyShortcode: string,
  preRegSurvey: Survey,
  siteContent: SiteContent,
  environmentName: string
}

export type PreregistrationResponse = {
  id: string
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
    const {shortcode, envName} = getEnvSpec()
    const response = await fetch(`${API_ROOT}/portals/v1/${shortcode}/env/${envName}`, this.getGetInit())
    return await this.processJsonResponse(response)
  },

  /** submit portal preregistration survey data */
  async completePortalPreReg({surveyStableId, surveyVersion, preRegResponse}:
                               {
                                 surveyStableId: string, surveyVersion: number,
                                 preRegResponse: DenormalizedPreRegResponse
                               }):
    Promise<PreregistrationResponse> {
    const {shortcode, envName} = getEnvSpec()
    const url = `${API_ROOT}/portals/v1/${shortcode}/env/${envName}/preReg/${surveyStableId}/${surveyVersion}`
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
  async confirmStudyPreEnroll(preEnroll: string, studyShortcode: string):
    Promise<void> {
    const {shortcode, envName} = getEnvSpec()
    const url = `${API_ROOT}/portals/v1/${shortcode}/env/${envName}/studies/${studyShortcode}`
      + `/preEnroll/${preEnroll}/confirm`
    const response = await fetch(url, {headers: this.getInitHeaders()})
    if (!response.ok) {
      return Promise.reject(response)
    }
  },

  /**
   * confirms that a client-side saved preregistration id is still valid.  For cases where the user refreshes the
   * page while on registration
   */
  async confirmPortalPreReg(preRegId: string):
    Promise<void> {
    const {shortcode, envName} = getEnvSpec()
    const url = `${API_ROOT}/portals/v1/${shortcode}/env/${envName}`
      + `/preReg/${preRegId}/confirm`
    const response = await fetch(url, {headers: this.getInitHeaders()})
    if (!response.ok) {
      return Promise.reject(response)
    }
  },

  /** submits registration data for a particular portal, from an anonymous user */
  async register({preRegResponseId, fullData}: { preRegResponseId: string, fullData: object }): Promise<object> {
    const {shortcode, envName} = getEnvSpec()
    let url = `${API_ROOT}/portals/v1/${shortcode}/env/${envName}/register`
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

  async unauthedLogin(username: string): Promise<ParticipantUser> {
    const url = `${API_ROOT}/current-user/v1/unauthed-login?${new URLSearchParams({
      username
    })}`
    const response = await fetch(url, {
      method: 'POST',
      headers: this.getInitHeaders()
    })
    return await this.processJsonResponse(response)
  },

  async tokenLogin(token: string): Promise<ParticipantUser> {
    const url = `${API_ROOT}/current-user/v1/token-login`
    const response = await fetch(url, {
      method: 'POST',
      headers: this.getInitHeaders(),
      body: JSON.stringify({token})
    })
    return await this.processJsonResponse(response)
  },

  setBearerToken(token: string | null) {
    bearerToken = token
  }

}

/**
 * Returns a url suitable for inclusion in an <img> tag based on a image shortcode
 */
export function getImageUrl(imageShortcode: string) {
  const {shortcode, envName} = getEnvSpec()
  return `${API_ROOT}/portals/v1/${shortcode}/env/${envName}/siteImages/${imageShortcode}`
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

