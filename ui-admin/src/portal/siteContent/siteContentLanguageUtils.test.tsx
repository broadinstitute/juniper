import {
  mockHtmlPage,
  mockNavbarItem,
  mockSiteContent
} from '../../test-utils/mock-site-content'
import {
  NavbarItemMailingList,
  SiteContent
} from '@juniper/ui-core'
import {
  extractAllTexts,
  extractSectionTexts,
  languageExtractToCSV,
  languageImportFromCSV
} from './siteContentLanguageUtils'

test('produces language extract from SiteContent', async () => {
  const siteContent: SiteContent = {
    ...mockSiteContent(),
    localizedSiteContents: [
      {
        ...mockSiteContent().localizedSiteContents[0],
        landingPage: {
          ...mockHtmlPage(),
          sections: [
            {
              id: '',
              sectionType: 'HERO_CENTERED',
              sectionConfig: JSON.stringify({
                title: 'about us',
                blurb: 'we are the best'
              })
            }
          ]
        },
        pages: []
      }
    ]
  }
  const extracts = extractAllTexts(siteContent)
  expect(extracts).toEqual([
    {
      language: 'en',
      textMap: {
        'landingPage.sections[0].parsedConfig.title': 'about us',
        'landingPage.sections[0].parsedConfig.blurb': 'we are the best'
      }
    }
  ])
})

test('HeroCentered produces language extract', async () => {
  const languageExtract = extractSectionTexts({
    id: '',
    sectionType: 'HERO_CENTERED',
    sectionConfig: JSON.stringify({
      title: 'about us',
      blurb: 'we are the best'
    })
  }, 'prefix', 'en')
  expect(languageExtract.textMap).toEqual({
    'prefix.parsedConfig.title': 'about us',
    'prefix.parsedConfig.blurb': 'we are the best'
  })
})

test('HeroWithImage produces language extract', async () => {
  const languageExtract = extractSectionTexts({
    id: '',
    sectionType: 'HERO_WITH_IMAGE',
    sectionConfig: JSON.stringify({
      title: 'about us',
      blurb: 'we are the best',
      buttons: [
        { text: 'click me' }
      ],
      image: {
        alt: 'imageAltText'
      }
    })
  }, 'prefix', 'en')
  expect(languageExtract.textMap).toEqual({
    'prefix.parsedConfig.title': 'about us',
    'prefix.parsedConfig.buttons[0].text': 'click me',
    'prefix.parsedConfig.image.alt': 'imageAltText',
    'prefix.parsedConfig.blurb': 'we are the best'
  })
})

test('produces language extract from LinkSectionsFooter', async () => {
  const languageExtract = extractSectionTexts({
    id: '',
    sectionType: 'LINK_SECTIONS_FOOTER',
    sectionConfig: JSON.stringify({
      itemSections: [{
        title: 'about us',
        items: [{
          text: 'link1'
        }, {
          text: 'link2'
        }]
      }]
    })
  }, 'prefix', 'en')
  expect(languageExtract.textMap).toEqual({
    'prefix.parsedConfig.itemSections[0].items[0].text': 'link1',
    'prefix.parsedConfig.itemSections[0].items[1].text': 'link2',
    'prefix.parsedConfig.itemSections[0].title': 'about us'
  })
})

test('imports a csv with a language', async () => {
  const siteContent: SiteContent = {
    ...mockSiteContent(),
    localizedSiteContents: [
      {
        ...mockSiteContent().localizedSiteContents[0],
        landingPage: {
          ...mockHtmlPage(),
          sections: [
            {
              id: '',
              sectionType: 'HERO_CENTERED',
              sectionConfig: JSON.stringify({
                title: 'about us',
                blurb: 'we are the best'
              })
            }
          ]
        },
        pages: [
          {
            ...mockHtmlPage(),
            sections: [
              {
                id: '',
                sectionType: 'HERO_CENTERED',
                sectionConfig: JSON.stringify({
                  title: 'participation',
                  blurb: 'join now'
                })
              }
            ]
          }
        ],
        navbarItems: [{
          ...mockNavbarItem(),
          itemType: 'MAILING_LIST',
          text: 'mailing list'
        }]
      }
    ]
  }
  const csvString = languageExtractToCSV(siteContent)

  let updatedCsv = csvString.replace('about us', 'about them')
  updatedCsv = updatedCsv.replace('we are the best', 'we are the worst')
  updatedCsv = updatedCsv.replace('join now', 'join later')
  updatedCsv = updatedCsv.replace('mailing list', 'dont contact me')
  const updatedContent = languageImportFromCSV(siteContent, updatedCsv)
  expect(updatedContent.localizedSiteContents[0].landingPage.sections[0].sectionConfig)
    .toEqual('{"title":"about them","blurb":"we are the worst"}')
  expect((updatedContent.localizedSiteContents[0].navbarItems[0] as NavbarItemMailingList)
    .text).toEqual('dont contact me')
  expect(updatedContent.localizedSiteContents[0].pages[0].sections[0].sectionConfig)
    .toEqual('{"title":"participation","blurb":"join later"}')
})
