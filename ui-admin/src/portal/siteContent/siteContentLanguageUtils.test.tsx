import { mockHtmlPage, mockSiteContent } from '../../test-utils/mock-site-content'
import { SiteContent } from '@juniper/ui-core'
import { extractAllTexts, extractSectionTexts } from './siteContentLanguageUtils'

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
        }
      }
    ]
  }
  const extracts = extractAllTexts(siteContent)
  expect(extracts).toEqual([
    {
      language: 'en',
      textMap: {
        'landing.section[0].title': 'about us',
        'landing.section[0].blurb': 'we are the best'
      }
    }
  ])
})

test('HeroCentered produces language extract', async () => {
  const texts = extractSectionTexts({
    id: '',
    sectionType: 'HERO_CENTERED',
    sectionConfig: JSON.stringify({
      title: 'about us',
      blurb: 'we are the best'
    })
  }, 'prefix')
  expect(texts).toEqual({
    'prefix.title': 'about us',
    'prefix.blurb': 'we are the best'
  })
})

test('HeroWithImage produces language extract', async () => {
  const texts = extractSectionTexts({
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
  }, 'prefix')
  expect(texts).toEqual({
    'prefix.title': 'about us',
    'prefix.buttons[0].text': 'click me',
    'prefix.image.alt': 'imageAltText',
    'prefix.blurb': 'we are the best'
  })
})

test('produces language extract from LinkSectionsFooter', async () => {
  const texts = extractSectionTexts({
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
  }, 'prefix')
  expect(texts).toEqual({
    'prefix.itemSections[0].items[0].text': 'link1',
    'prefix.itemSections[0].items[1].text': 'link2',
    'prefix.itemSections[0].title': 'about us'
  })
})

