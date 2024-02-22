import React from 'react'
import { render, waitFor } from '@testing-library/react'
import I18nProvider, { useI18n } from './I18nProvider'
import { useUser } from './UserProvider'

jest.mock('api/api', () => ({
  getLanguageTexts: () => {
    return Promise.resolve([{ messageKey: 'testKey', text: 'Test Text', language: 'en' }])
  }
}))

jest.mock('providers/UserProvider', () => ({ useUser: jest.fn() }))

beforeEach(() => {
  // @ts-expect-error TS doesn't realize this function is mocked
  useUser.mockReturnValue({
    selectedLanguage: 'en'
  })
})

describe('I18nProvider', () => {
  it('loads language texts based on the selected language', async () => {
    const TestComponent = () => {
      const { i18n } = useI18n()
      return <span>{i18n('testKey')}</span>
    }

    const { getByText } = render(
      <I18nProvider>
        <TestComponent />
      </I18nProvider>
    )

    await waitFor(() => {
      expect(getByText('Test Text')).toBeInTheDocument()
    })
  })
})
