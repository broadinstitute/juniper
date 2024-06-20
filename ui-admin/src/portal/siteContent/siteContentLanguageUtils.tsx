import { HtmlPage, LocalSiteContent, SiteContent } from '@juniper/ui-core'
import { extractMessagesAndKeys as extractHeroWithImageTemplate } from
  '@juniper/ui-core/src/participant/landing/sections/HeroWithImageTemplate'
import { extractMessagesAndKeys as extractLinkSectionsFooter } from
  '@juniper/ui-core/build/participant/landing/sections/LinkSectionsFooter'


/**
 *
 */
export function extractAllTexts(siteContent: SiteContent) {
  const allTexts: Record<string, string>[] = []
  siteContent.localizedSiteContents.forEach(lsc => {
    allTexts.push(extractAllLocalTexts(lsc))
  })
  return allTexts
}

/**
 *
 */
export function extractAllLocalTexts(lsc: LocalSiteContent) {
  let texts: Record<string, string> = {}
  if (lsc.landingPage) {
    extractAllPageTexts(lsc.landingPage, 'landing')
  }
  lsc.navbarItems.forEach((navbarItem, index) => {
    texts[`navbar[${index}].text`] = navbarItem.text
    if (navbarItem.itemType === 'INTERNAL') {
      texts = { ...texts, ...extractAllPageTexts(navbarItem.htmlPage, `subpage[${index}]`) }
    }
  })
  return texts
}

/**
 *
 */
export function extractAllPageTexts(page: HtmlPage, prefix: string) {
  const texts: Record<string, string> = {}
  page.sections.forEach(section => {
    // @ts-ignore
    const extractor = sectionExtractors[section.sectionType]
    if (extractor) {
      const texts = extractor(section.sectionConfig, prefix)
    }
  })
  return texts
}

const sectionExtractors = {
  'HERO_WITH_IMAGE': extractHeroWithImageTemplate,
  'LINK_SECTIONS_FOOTER': extractLinkSectionsFooter
}
