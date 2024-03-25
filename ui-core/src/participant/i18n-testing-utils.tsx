import React from 'react'
import { I18nContext, I18nContextT } from './I18nProvider'

export const mockTextsDefault: Record<string, string> = { taskTypeConsent: 'Consent', start: 'Start' }

/**
 * Returns a MockI18nProvider. Used to test components that need an I18nContext
 */
export const MockI18nProvider = ({ children, mockTexts = {}, selectedLanguage = 'en' }: {
    children: React.ReactNode, mockTexts?: Record<string, string>, selectedLanguage?: string
}) => {
  const fakeI18nContext: I18nContextT = {
    languageTexts: mockTexts,
    selectedLanguage,
    // eslint-disable-next-line @typescript-eslint/no-empty-function
    changeLanguage: () => {},
    i18n: (key: string) => (Object.hasOwn(mockTexts, key) ? mockTexts[key] : `{${key}}`)
  }
  return <I18nContext.Provider value={fakeI18nContext}>
    {children}
  </I18nContext.Provider>
}
