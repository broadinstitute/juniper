import React from 'react'
import { I18nContextT, I18nContext } from './I18nProvider'

export const mockTextsDefault: Record<string, string> = { taskTypeConsent: 'Consent', taskStart: 'Start' }

/**
 * Returns a MockI18nProvider. Used to test components that need an I18nContext
 */
export const MockI18nProvider = ({ children, mockTexts }: {
    children: React.ReactNode, mockTexts: Record<string, string>
}) => {
  const fakeI18nContext: I18nContextT = {
    languageTexts: mockTexts,
    selectedLanguage: 'en',
    // eslint-disable-next-line @typescript-eslint/no-empty-function
    changeLanguage: () => {},
    i18n: (key: string) => mockTexts[key]
  }
  return <I18nContext.Provider value={fakeI18nContext}>
    {children}
  </I18nContext.Provider>
}
