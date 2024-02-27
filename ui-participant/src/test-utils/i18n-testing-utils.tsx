import React from 'react'
import { I18nContext, I18nContextT } from 'providers/I18nProvider'

export const mockTextsDefault: Record<string, string> = { taskTypeConsent: 'Consent', taskStart: 'Start' }

/**
 * Returns a MockI18nProvider. Used to test components that need an I18nContext
 */
export const MockI18nProvider = ({ children, mockTexts }: {
    children: React.ReactNode, mockTexts: Record<string, string>
}) => {
  const fakeI18nContext: I18nContextT = {
    languageTexts: mockTexts,
    i18n: (key: string) => mockTexts[key]
  }
  return <I18nContext.Provider value={fakeI18nContext}>
    {children}
  </I18nContext.Provider>
}
