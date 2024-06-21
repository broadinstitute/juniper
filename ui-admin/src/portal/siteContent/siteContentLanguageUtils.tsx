import {
  HtmlPage, LocalSiteContent, SiteContent, allSectionProps
} from '@juniper/ui-core'
import { SectionProp } from '@juniper/ui-core/build/participant/landing/sections/SectionProp'

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
  const texts: Record<string, string> = {}
  if (lsc.landingPage) {
    Object.assign(texts, extractAllPageTexts(lsc.landingPage, 'landing'))
  }
  lsc.navbarItems.forEach((navbarItem, index) => {
    texts[`navbar[${index}].text`] = navbarItem.text

    if (navbarItem.itemType === 'INTERNAL') {
      Object.assign(texts, extractAllPageTexts(navbarItem.htmlPage, `page[${navbarItem.htmlPage.path}]`))
    }
  })
  return texts
}

/**
 *
 */
export function extractAllPageTexts(page: HtmlPage, prefix: string) {
  const texts: Record<string, string> = {}
  page.sections.forEach((section, sectionIndex) => {
    // @ts-ignore
    const sectionProps = allSectionProps[section.sectionType]
    if (sectionProps && section.sectionConfig) {
      Object.assign(texts, extractConfigTexts(JSON.parse(section.sectionConfig) as Record<string, unknown>,
        sectionProps,
        `${prefix}.section[${sectionIndex}]`))
    }
  })
  return texts
}

/**
 *
 */
export function extractConfigTexts(sectionConfig: Record<string, unknown>, sectionProps: SectionProp[], prefix: string):
  Record<string, string> {
  const texts: Record<string, string> = {}
  sectionProps.forEach(prop => {
    if (prop.translated) {
      // @ts-ignore
      texts[`${prefix}.${prop.name}`] = sectionConfig[prop.name]
    }
    if (prop.subProps && sectionConfig[prop.name]) {
      if (prop.isArray) {
        // @ts-ignore
        sectionConfig[prop.name].forEach((item: unknown, index: number) => {
          Object.assign(texts, extractConfigTexts(item as Record<string, unknown>,
            prop.subProps!, `${prefix}.${prop.name}[${index}]`))
        })
      } else {
        Object.assign(texts, extractConfigTexts(sectionConfig[prop.name] as Record<string, unknown>,
          prop.subProps, `${prefix}.${prop.name}`))
      }
    }
  })
  return texts
}
