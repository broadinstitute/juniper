import {
  AddressValidationResult,
  HtmlPage,
  LocalSiteContent,
  NavbarItem,
  SectionType,
  SiteContent
} from 'api/api'
import {
  ApiContextT,
  HtmlSection,
  HubResponse,
  SystemSettings
} from '@juniper/ui-core'

/** mock site content */
export const mockSiteContent = (): SiteContent => {
  return {
    id: 'fakeId1',
    stableId: 'fakeId1',
    version: 1,
    localizedSiteContents: [
      mockLocalSiteContent()
    ],
    createdAt: 0
  }
}

/** mock local site content */
export const mockLocalSiteContent = (): LocalSiteContent => {
  return {
    navbarItems: [],
    language: 'en',
    primaryBrandColor: '#3be',
    pages: [
      mockHtmlPage()
    ],
    landingPage: mockLandingPage(),
    navLogoVersion: 1,
    navLogoCleanFileName: 'fakeLogo.png',
    languageTextOverrides: []
  }
}

export const mockLandingPage = (): HtmlPage => {
  return {
    sections: [mockHtmlSection()]
  } as HtmlPage
}

/** mock html page */
export const mockHtmlPage = (): HtmlPage => {
  return {
    path: '/',
    sections: [mockHtmlSection()],
    title: 'example page',
    minimalNavbar: false
  }
}

/** mock nav iteme */
export const mockNavbarItem= (): NavbarItem => {
  return {
    text: 'example page',
    itemType: 'EXTERNAL',
    itemOrder: 1,
    href: 'https://example.com'
  }
}

/** mock single page section */
export const mockHtmlSection = (): HtmlSection => {
  return {
    id: 'fakeId',
    sectionType: 'HERO_CENTERED',
    sectionConfig: JSON.stringify({
      title: 'about us',
      blurb: 'we are the best'
    })
  }
}

/**
 * Returns an empty html section of the given type
 */
export const makeEmptyHtmlSection = (sectionType: SectionType): HtmlSection => {
  return {
    id: 'fakeId',
    sectionType,
    sectionConfig: JSON.stringify({})
  }
}

/** no-op apiContext for rendering preview participant content in tests */
export const emptyApi: ApiContextT = {
  getImageUrl: () => '',
  submitMailingListContact: () => Promise.resolve({}),
  getLanguageTexts: () => Promise.resolve({}),
  updateSurveyResponse: () => Promise.resolve({} as HubResponse),
  validateAddress: () => Promise.resolve({} as AddressValidationResult),
  loadSystemSettings: () => Promise.resolve({} as SystemSettings)
}
