export type PortalEnvironmentParams = {
  studyShortname: string,
  environmentName: string
}

export type PortalEnvironment = PortalEnvironmentParams & {
  siteContent: SiteContent
}

export type SiteContent = {
  defaultLanguage: string,
  localizedSiteContents: LocalSiteContent[],
}

export type LocalSiteContent = {
  language: string,
  navbarItems: NavbarItem[],
  landingPage: HtmlPage
}

export type HtmlPage = {
  title: string,
  path: string,
  sections: HtmlSection[]
}

export type NavbarItem = {
  label: string,
  externalLink: string,
  navbarItemType: string
  htmlPage: HtmlPage
}

export type HtmlSection = {
  id: string,
  sectionType: string,
  rawContent: string | null,
  sectionConfig: string | null
}

export type ButtonConfig = { text: string, href: string }

// eslint-disable-next-line  @typescript-eslint/no-explicit-any
export type SectionConfig = { [index: string]: any }

const bearerToken: string | null = null
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

  async getPortalEnvironment(portalShortcode: string, envName: string): Promise<PortalEnvironment> {
    const response = await fetch(`${API_ROOT}/portals/v1/${portalShortcode}/env/${envName}`, this.getGetInit())
    return await this.processJsonResponse(response)
  }
}

/**
 * Returns a url suitable for inclusion in an <img> tag based on a image shortcode
 */
export function getImageUrl(imageShortcode: string) {
  const { shortname, envName } = getEnvSpec()
  return `${API_ROOT}/portals/v1/${shortname}/env/${envName}/siteImages/${imageShortcode}`
}

export type EnvSpec = {
  shortname: string,
  envName: string
}

/** gets the current environment params */
export function getEnvSpec(): EnvSpec {
  return readEnvFromHostname(window.location.hostname)
}

/** parses shortcode and environment from hostname */
function readEnvFromHostname(hostname: string): EnvSpec {
  let shortname; let envName = ''
  const splitHostname = hostname.split('.')
  if (Object.keys(ALLOWED_ENV_NAMES).includes(splitHostname[0])) {
    envName = ALLOWED_ENV_NAMES[splitHostname[0]]
    shortname = splitHostname[1]
  } else {
    envName = 'LIVE'
    shortname = splitHostname[0]
  }
  return { envName, shortname }
}

const ALLOWED_ENV_NAMES: Record<string, string> = {
  'sandbox': 'SANDBOX',
  'irb': 'IRB',
  'live': 'LIVE'
}

