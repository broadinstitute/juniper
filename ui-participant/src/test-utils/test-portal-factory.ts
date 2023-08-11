import {
  HtmlPage,
  LocalSiteContent,
  Portal,
  PortalEnvironment,
  PortalEnvironmentConfig,
  SiteContent
} from '@juniper/ui-core'

export const mockPortal = (): Portal => {
  return {
    name: 'mock portal',
    id: 'portal123',
    shortcode: 'mockportal',
    portalStudies: [],
    portalEnvironments: [mockPortalEnvironment()]
  }
}

export const mockPortalEnvironment = (): PortalEnvironment => {
  return {
    environmentName: 'sandbox',
    portalEnvironmentConfig: mockPortalEnvironmentConfig(),
    siteContent: mockSiteContent()
  }
}

export const mockPortalEnvironmentConfig = (): PortalEnvironmentConfig => {
  return {
    acceptingRegistration: true,
    initialized: true,
    password: 'password',
    passwordProtected: false
  }
}

export const mockSiteContent = (): SiteContent => {
  return {
    defaultLanguage: 'en',
    localizedSiteContents: [mockLocalSiteContent()],
    stableId: 'mockContent',
    version: 1
  }
}

export const mockLocalSiteContent = (): LocalSiteContent => {
  return {
    language: 'en',
    navbarItems: [],
    landingPage: mockHtmlPage(),
    navLogoCleanFileName: 'navLogo.png',
    navLogoVersion: 1
  }
}

export const mockHtmlPage = (): HtmlPage => {
  return {
    title: 'mock home page',
    path: '/',
    sections: []
  }
}
