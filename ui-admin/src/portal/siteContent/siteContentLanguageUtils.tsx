import {
  allSectionProps,
  HtmlSection,
  SectionProp,
  SiteContent
} from '@juniper/ui-core'
import _union from 'lodash/union'
import { escapeCsvValue } from 'util/downloadUtils'

import Papa from 'papaparse'
import _set from 'lodash/set'
import _cloneDeep from 'lodash/cloneDeep'
import _uniq from 'lodash/uniq'

const KEY_HEADER = 'key'

/** take the textMaps and reformat into a CSV with one row per key, and one language per column */
export function languageExtractToCSV(siteContent: SiteContent): string {
  const languageExtracts = extractAllTexts(siteContent)
  const headerRow = [KEY_HEADER, ...languageExtracts.map(extract => extract.language)]
  const allKeys = _union(...languageExtracts.map(extract => Object.keys(extract.textMap)))
  const rows = allKeys.map(key =>
    [key, ...languageExtracts.map(extract => escapeCsvValue(extract.textMap[key] ?? ''))]
  )
  return [headerRow, ...rows].map(row => row.join(',')).join('\n')
}

type LanguageExtract = {
  textMap: Record<string, string>
  language: string
}

/**
 * pulls all translated texts from a site content object.  Returns an array with one object per language.
 */
export function extractAllTexts(siteContent: SiteContent): LanguageExtract[] {
  const extracts = processSectionConfigs(siteContent, extractSectionTexts)
  // Outside of the configs, the the navbarItem texts are the only texts needing i18n
  siteContent.localizedSiteContents.forEach(lsc => {
    lsc.navbarItems.forEach((navbarItem, index)  => {
      extracts.push({
        textMap: { [`navbarItems[${index}].text`]: navbarItem.text },
        language: lsc.language
      })
    })
  })

  const languages = _uniq(extracts.map(extract => extract.language))
  const combinedExtracts = languages.map(language => {
    const textMap: Record<string, string> = {}
    extracts.filter(extract => extract.language === language)
      .forEach(extract => Object.assign(textMap, extract.textMap))
    return { textMap, language }
  })
  return combinedExtracts
}

/**
 * gets a map of all translated texts in a section
 */
export function extractSectionTexts(section: HtmlSection, prefix: string, language: string): LanguageExtract {
  // @ts-ignore
  const sectionProps = allSectionProps[section.sectionType]
  if (sectionProps && section.sectionConfig) {
    return {
      textMap: extractConfigTexts(JSON.parse(section.sectionConfig) as Record<string, unknown>,
        sectionProps,
        `${prefix}.parsedConfig`),
      language
    }
  }
  return { textMap: {}, language }
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
export function languageImportFromCSV(siteContent: SiteContent, csvString: string): SiteContent {
  // parse the config strings to objects
  const workingContent = _cloneDeep(siteContent)
  parseJsonConfigs(workingContent)
  const { data } = Papa.parse<string[]>(csvString, {})
  const languages: string[] = (data[0])!.slice(1)
  data.forEach(row => {
    processCsvRow(row, workingContent, languages)
  })
  stringifyJsonConfigs(workingContent)
  return workingContent
}

/**
 * expands the section configs from strings to objects
 */
export function parseJsonConfigs(siteContent: SiteContent): void {
  processSectionConfigs(siteContent, section => {
    section.parsedConfig = section.sectionConfig ? JSON.parse(section.sectionConfig) : section.sectionConfig
  })
}


/**
 * expands the section configs from strings to objects
 */
export function stringifyJsonConfigs(siteContent: SiteContent): void {
  processSectionConfigs(siteContent, section => {
    section.sectionConfig = section.parsedConfig ? JSON.stringify(section.parsedConfig) : section.sectionConfig
  })
}

/**
 * take a row from a CSV and set the values in the siteContent object
 */
export function processCsvRow(row: string[], siteContent: SiteContent, languages: string[]): void {
  const key = row[0]
  row.slice(1).forEach((value, index) => {
    const lsc = siteContent.localizedSiteContents
      .find(lsc => lsc.language === languages[index])
    if (lsc) {
      _set(lsc, key, value)
    }
  })
}

type ConfigProcessor<T> = (config: HtmlSection, path: string, language: string) => T
/**
 * applies the function to each section config in the site content
 */
export function processSectionConfigs<T>(siteContent: SiteContent, processor: ConfigProcessor<T>): T[] {
  const results: T[] = []
  siteContent.localizedSiteContents.forEach(lsc => {
    if (lsc.landingPage) {
      lsc.landingPage.sections.forEach((section, index) => {
        results.push(processor(section, `landingPage.sections[${index}]`, lsc.language))
      })
    }
    if (lsc.footerSection) {
      results.push(processor(lsc.footerSection, 'footerSection', lsc.language))
    }
    lsc.pages.forEach((page, pageIndex) => {
      page.sections.forEach((section, sectionIndex) => {
        results.push(processor(section, `pages[${pageIndex}].sections[${sectionIndex}]`, lsc.language))
      })
    })
  })
  return results
}
