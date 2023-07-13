import { SiteContent, LocalSiteContent, HtmlPage } from 'api/api'
import { HtmlSection } from '@juniper/ui-core/build/types/landingPageConfig'

export const mockSiteContent = (): SiteContent => {
  return {
    defaultLanguage: 'en',
    stableId: 'fakeId1',
    version: 1,
    localizedSiteContents: [
      mockLocalSiteContent()
    ]
  }
}

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

export const mockHtmlPage = (): HtmlPage => {
  return {
    path: '/',
    sections: [mockHtmlSection()],
    title: 'example page'
  }
}

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

export const emptyApi = {
  getImageUrl: () => '',
  submitMailingListContact: () => Promise.resolve({})
}
