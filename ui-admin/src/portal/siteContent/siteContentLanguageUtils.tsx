import {
  HtmlPage, LocalSiteContent, SiteContent, allSectionProps, HtmlSection
} from '@juniper/ui-core'
import { SectionProp } from '@juniper/ui-core/build/participant/landing/sections/SectionProp'
import _union from 'lodash/union'
import { escapeCsvValue } from 'util/downloadUtils'
import * as csv from 'fast-csv'
import _set from 'lodash/set'

const KEY_HEADER = 'key'

/** take the textMaps and reformat into a CSV with one row per key, and one language per column */
export function languageExtractToCSV(languageExtract: LanguageExtract[]): string {
  const headerRow = [KEY_HEADER, ...languageExtract.map(extract => extract.language)]
  const allKeys = _union(...languageExtract.map(extract => Object.keys(extract.textMap)))
  const rows = allKeys.map(key =>
    [key, ...languageExtract.map(extract => escapeCsvValue(extract.textMap[key] ?? ''))]
  )
  return [headerRow, ...rows].map(row => row.join(',')).join('\n')
}


/**
 * pulls all translated texts from a site content object.  Returns an array with one object per language.
 */
export function extractAllTexts(siteContent: SiteContent): LanguageExtract[] {
  return siteContent.localizedSiteContents.map(extractAllLocalTexts)
}

type LanguageExtract = {
  textMap: Record<string, string>
  language: string
}

/**
 * gets a map of all translated texts in a LocalSiteContent object
 */
export function extractAllLocalTexts(lsc: LocalSiteContent): LanguageExtract {
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
  return {
    textMap: texts,
    language: lsc.language
  }
}

/**
 * gets a map of all translated texts in a page
 */
export function extractAllPageTexts(page: HtmlPage, prefix: string) {
  const texts: Record<string, string> = {}
  page.sections.forEach((section, sectionIndex) => {
    Object.assign(texts, extractSectionTexts(section, `${prefix}.section[${sectionIndex}]`))
  })
  return texts
}

/**
 * gets a map of all translated texts in a section
 */
export function extractSectionTexts(section: HtmlSection, prefix: string) {
  // @ts-ignore
  const sectionProps = allSectionProps[section.sectionType]
  if (sectionProps && section.sectionConfig) {
    return extractConfigTexts(JSON.parse(section.sectionConfig) as Record<string, unknown>,
      sectionProps,
      prefix)
  }
}

/**
 * gets a map of all translated texts in a section configuration
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

/** take a csv as string and import it to the siteContent */
export function languageImportFromCSV(siteContent: SiteContent, csvString: string): void {
  csv.parseString(csvString, { headers: true  })
    .on('data', row => parseRow(row, siteContent, [])
    )
}

/**
 *
 */
export function parseRow(row: Record<string, string>, siteContent: SiteContent): void {
  Object.keys(row).forEach(language => {
    const lsc = siteContent.localizedSiteContents.find(lsc => lsc.language === language)
    if (lsc) {
      _set(lsc, row[KEY_HEADER], row[language])
    }
  })
}
