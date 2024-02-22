import React, { createContext, useContext, useEffect, useState } from 'react'
import { LanguageText } from '@juniper/ui-core'
import Api from '../api/api'
import { usePortalEnv } from './PortalProvider'
import { useUser } from './UserProvider'

const I18nContext = createContext<I18nContextT | null>(null)

export type I18nContextT = {
  languageTexts: LanguageText[]
  i18n: (key: string) => string
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
    languageTexts: i18nContext.languageTexts
  }
}

/**
 * Provider for the current portal environment's i18n context.
 */
export default function I18nProvider({ children }: { children: React.ReactNode }) {
  const { portal, portalEnv } = usePortalEnv()
  const { selectedLanguage } = useUser()
  const [isLoading, setIsLoading] = useState(true)
  const [isError, setIsError] = useState(false)
  const [languageTexts, setLanguageTexts] = useState<LanguageText[]>([])

  useEffect(() => {
    reloadLanguageTexts(portal.shortcode, portalEnv.environmentName, selectedLanguage)
  }, [])

  const reloadLanguageTexts = (portalShortcode: string, envName: string, selectedLanguage: string) => {
    setIsLoading(true)
    Api.getLanguageTexts(selectedLanguage).then(result => {
      setLanguageTexts(result)
      setIsError(false)
      setIsLoading(false)
    }).catch(() => {
      setIsError(true)
      setIsLoading(false)
    })
  }

  const i18n = (key: string) => {
    return languageTexts.find(text => text.messageKey === key)?.text || `{${key}}`
  }

  return <>
    {isLoading && <div className="bg-white h-100 w-100">
      <div className="position-absolute top-50 start-50 translate-middle">Loading...</div>
    </div>}
    {isError && <div className="bg-white h-100 w-100">
      <div className="position-absolute top-50 start-50 translate-middle text-center">
        There is no Juniper site configured for this url.<br/>
        If this is an error, contact <a href="mailto:support@juniper.terra.bio">support@juniper.terra.bio</a>.
      </div>
    </div>}
    {!isLoading && !isError && <I18nContext.Provider value={{ languageTexts, i18n }}>
      {children}
    </I18nContext.Provider>}
  </>
}
