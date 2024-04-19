import {
  HtmlPage,
  LocalSiteContent,
  Portal,
  PortalEnvironment,
  PortalEnvironmentConfig,
  SiteContent, Study, StudyEnvironment
} from '@juniper/ui-core'

/** mock portal object with one environment */
export const mockPortal = (): Portal => {
  return {
    name: 'mock portal',
    id: 'portal123',
    shortcode: 'mockportal',
    portalStudies: [],
    portalEnvironments: [mockPortalEnvironment()]
  }
}

/** mock study object */
export const mockStudy = (): Study => {
  return {
    shortcode: 'study1',
    name: 'Study 1',
    studyEnvironments: []
  }
}

/** mock study environment object */
export const mockStudyEnv = (): StudyEnvironment => {
  return {
    id: 'studyEnv1',
    environmentName: 'sandbox',
    studyEnvironmentConfig: {
      acceptingEnrollment: true,
      initialized: true,
      passwordProtected: false,
      password: 'password'
    },
    configuredSurveys: [],
    configuredConsents: [],
    triggers: []
  }
}


/** mock environment with a siteContent */
export const mockPortalEnvironment = (): PortalEnvironment => {
  return {
    environmentName: 'sandbox',
    portalEnvironmentConfig: mockPortalEnvironmentConfig(),
    siteContent: mockSiteContent(),
    supportedLanguages: [{ languageCode: 'en', languageName: 'English' }]
  }
}

/** mock config with everything open */
export const mockPortalEnvironmentConfig = (): PortalEnvironmentConfig => {
  return {
    acceptingRegistration: true,
    initialized: true,
    password: 'password',
    passwordProtected: false,
    defaultLanguage: 'en'
  }
}

/** mock site content with one localization */
export const mockSiteContent = (): SiteContent => {
  return {
    id: 'fakeID1',
    localizedSiteContents: [mockLocalSiteContent()],
    stableId: 'mockContent',
    version: 1,
    createdAt: 0
  }
}

/** mock local content with a single empty landing page */
export const mockLocalSiteContent = (): LocalSiteContent => {
  return {
    language: 'en',
    navbarItems: [],
    landingPage: mockHtmlPage(),
    navLogoCleanFileName: 'navLogo.png',
    navLogoVersion: 1
  }
}

/** mock empty page */
export const mockHtmlPage = (): HtmlPage => {
  return {
    title: 'mock home page',
    path: '/',
    sections: []
  }
}

/**
 * Returns a mock object that can be returned by the usePortalEnv hook
 */
export const mockUsePortalEnv = () => {
  return {
    portal: mockPortal(),
    reloadPortal: jest.fn(),
    portalEnv: mockPortalEnvironment(),
    localContent: mockLocalSiteContent()
  }
}
