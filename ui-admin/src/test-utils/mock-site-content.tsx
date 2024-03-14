import { SiteContent, LocalSiteContent, HtmlPage } from 'api/api'
import { HtmlSection } from '@juniper/ui-core/build/types/landingPageConfig'

/** mock site content */
export const mockSiteContent = (): SiteContent => {
  return {
    id: 'fakeId1',
    defaultLanguage: 'en',
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
    landingPage: mockHtmlPage(),
    navLogoVersion: 1,
    navLogoCleanFileName: 'fakeLogo.png'
  }
}

/** mock html page */
export const mockHtmlPage = (): HtmlPage => {
  return {
    path: '/',
    sections: [mockHtmlSection()],
    title: 'example page'
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

/** no-op apiContext for rendering preview participant content in tests */
export const emptyApi = {
  getImageUrl: () => '',
  submitMailingListContact: () => Promise.resolve({}),
  getLanguageTexts: () => Promise.resolve({})
}
