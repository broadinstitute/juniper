import { isNil, sortBy, uniqBy } from 'lodash'

/**
 * Turns a list of 2-alpha country codes (e.g. US, MX, CA) to their
 * full country name in the given language. It will return a list
 * with the same order as the inputted list, but if a country was
 * not identified, it will be `undefined`.
 */
export const getCountryNames = (countryCodes: string[], lang = 'en') : (string | undefined)[] => {
  // automatically gets internationalized country names based upon the given language
  const names = new Intl.DisplayNames([lang], { type: 'region' })

  return countryCodes.map(code => {
    return names.of(code)
  })
}


const alphabet = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'
const fakeCountryCodes = [
  'ZZ', // maps to 'Unknown'
  'XA', // used for debugging alternate text formats
  'XB', // same as XA
  'UN', // used for 'United Nations' - not a country?
  'EU', // ^ 'european union'
  'EZ'  // ^ 'eurozone'
]
/**
 * Gets a sorted list of all countries with the given language
 */
export const getAllCountries = (lang = 'en') : { code: string, name: string }[] => {
  const out: { code: string, name: string }[] = []

  const names = new Intl.DisplayNames([lang], { type: 'region' })
  for (const char1 of alphabet) {
    for (const char2 of alphabet) {
      const code = char1 + char2
      if (fakeCountryCodes.includes(code)) {
        continue // there are country codes in the ICU database which are not real
      }

      const name = names.of(char1 + char2)

      if (!isNil(name) && name !== code) {
        out.push({
          code,
          name
        })
      }
    }
  }


  // ensure no duplicates (some countries seem to have multiple codes)
  // and sort
  return sortBy(uniqBy(out, val => val.name), val => val.name)
}
