import React, {
  createContext,
  useContext,
  useEffect,
  useState
} from 'react'
import { useApiContext } from './ApiProvider'
import { SUPPORT_EMAIL_ADDRESS } from '../util/supportUtils'
import { EnvironmentName } from 'src/types/study'
import { PortalEnvironmentLanguage } from 'src/types/portal'
import { useLocation } from 'react-router-dom'

export const I18nContext = createContext<I18nContextT | null>(null)

export type I18nOptions = {
  substitutions?: Record<string, string>,
  defaultValue?: string
}

export type I18nFn = (key: string, options?: I18nOptions) => string

export type I18nContextT = {
  languageTexts: Record<string, string>
  i18n: I18nFn,
  selectedLanguage: string,
  changeLanguage: (language: string) => void
}

/**
 * Returns a hook that allows the caller to internationalize a given piece of text.
 * Loads the language texts in the selected language.
 */
export function useI18n(): I18nContextT {
  const i18nContext = useContext(I18nContext)
  if (!i18nContext) {
    throw ('i18n context not initialized')
  }
  return {
    i18n: i18nContext.i18n,
    languageTexts: i18nContext.languageTexts,
    selectedLanguage: i18nContext.selectedLanguage,
    changeLanguage: i18nContext.changeLanguage
  }
}

const SELECTED_LANGUAGE_KEY = 'selectedLanguage'

/**
 * Provider for the current users i18n context.
 */
export function I18nProvider({
  defaultLanguage,
  portalShortcode,
  environmentName,
  loadedPortalContentLang,
  reloadPortalContent,
  children
}: {
  defaultLanguage: string,
  portalShortcode?: string,
  environmentName: EnvironmentName,
  children: React.ReactNode,
  loadedPortalContentLang?: string | null,
  reloadPortalContent?: (language: string) => void
}) {
  const Api = useApiContext()
  const [isLoading, setIsLoading] = useState(true)
  const [isError, setIsError] = useState(false)
  const [languageTexts, setLanguageTexts] = useState<Record<string, string>>({})
  const [selectedLanguage, setSelectedLanguage] = useState(
    localStorage.getItem(SELECTED_LANGUAGE_KEY) || defaultLanguage)

  const { pathname } = useLocation()

  const changeLanguage = (language: string) => {
    setSelectedLanguage(language)
    localStorage.setItem(SELECTED_LANGUAGE_KEY, language)
  }

  useEffect(() => {
    if (loadedPortalContentLang !== selectedLanguage && !pathname.includes('oauth')) {
      reloadPortalContent && reloadPortalContent(selectedLanguage)
    }
  }, [selectedLanguage, loadedPortalContentLang, pathname])

  useEffect(() => {
    reloadLanguageTexts(selectedLanguage)
  }, [selectedLanguage])

  const reloadLanguageTexts = (selectedLanguage: string) => {
    setIsLoading(true)
    Api.getLanguageTexts(selectedLanguage, portalShortcode, environmentName).then(result => {
      setLanguageTexts(result)
      setIsError(false)
      setIsLoading(false)
    }).catch(() => {
      setIsError(true)
      setIsLoading(false)
    })
  }

  const substitute = (text: string, substitutionKey: string, substitutions: Record<string, string>): string => {
    return text.replace(`\${${substitutionKey}}`, substitutions[substitutionKey])
  }

  const i18n = (key: string, options?: I18nOptions) => {
    let text = languageTexts[key] || options?.defaultValue || `{${key}}`
    if (options && options.substitutions) {
      for (const substitutionKey of Object.keys(options.substitutions)) {
        text = substitute(text, substitutionKey, options.substitutions)
      }
    }
    return text
  }

  return <>
    {isLoading && <div className="bg-white h-100 w-100">
      <div className="position-absolute top-50 start-50 translate-middle">Loading...</div>
    </div>}
    {isError && <div className="bg-white h-100 w-100">
      <div className="position-absolute top-50 start-50 translate-middle text-center">
            There is no Juniper site configured for this url.<br/>
            If this is an error, contact <a href={`mailto:${SUPPORT_EMAIL_ADDRESS}`}>{SUPPORT_EMAIL_ADDRESS}</a>.
      </div>
    </div>}
    {!isLoading && !isError && <I18nContext.Provider value={{ languageTexts, i18n, selectedLanguage, changeLanguage }}>
      {children}
    </I18nContext.Provider>}
  </>
}

//Juniper and B2C use slightly different language codes. For example, Simplified Chinese is "zh" in Juniper,
//but "zh-hans" in B2C. This mapping is used to convert Juniper language codes into B2C locale codes.
const JUNIPER_TO_B2C_LOCALE_MAP: Record<string, string> = {
  'en': 'en',
  'es': 'es',
  'de': 'de',
  'hi': 'hi',
  'ru': 'ru',
  'pt': 'pt-pt',
  'ja': 'ja',
  'it': 'it',
  'fr': 'fr',
  'pl': 'pl',
  'tr': 'tr',
  'zh': 'zh-hans',
  'dev': 'en' //our custom "dev" language should just use English in b2c
}

//This defaults to English to guarantee that B2C will function even if the language is unsupported.
//The user can always use in-browser translations if needed, and English is our most reliable language.
export const getB2CLocale = (key: string): string => {
  return JUNIPER_TO_B2C_LOCALE_MAP[key] || 'en'
}


export const inferBrowserDefaultLanguageCode = (languageOptions: PortalEnvironmentLanguage[]) => {
  const preferredLanguages = [
    navigator.language || '',
    ...(navigator.languages || [])
  ]

  for (const preferred of preferredLanguages) {
    const langCode = preferred.split('-')[0]

    if (languageOptions.find(opt => opt.languageCode === langCode)) {
      return langCode
    }
  }

  return 'en'// fallback
}
