import React from 'react'
import {
  I18nContext,
  I18nContextT,
  I18nOptions
} from './I18nProvider'

export const mockTextsDefault: Record<string, string> = { taskTypeConsent: 'Consent', start: 'Start' }

/**
 * Returns a MockI18nProvider. Used to test components that need an I18nContext
 */
export const MockI18nProvider = ({ children, mockTexts = {}, selectedLanguage = 'en', useDefaultTexts = false }: {
  children: React.ReactNode, mockTexts?: Record<string, string>, selectedLanguage?: string, useDefaultTexts?: boolean
}) => {
  const fakeI18nContext: I18nContextT = {
    languageTexts: mockTexts,
    selectedLanguage,
    // eslint-disable-next-line @typescript-eslint/no-empty-function
    changeLanguage: () => {},
    i18n: (key: string, opts?: I18nOptions) => {
      if (Object.hasOwn(mockTexts, key)) {
        return mockTexts[key]
      }

      if (useDefaultTexts && opts?.defaultValue) {
        return opts.defaultValue
      }

      return `{${key}}`
    }
  }
  return <I18nContext.Provider value={fakeI18nContext}>
    {children}
  </I18nContext.Provider>
}
