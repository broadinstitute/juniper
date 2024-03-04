import React from 'react'
import { render, waitFor } from '@testing-library/react'
import I18nProvider, { useI18n } from './I18nProvider'
import { useUser } from './UserProvider'

jest.mock('api/api', () => ({
  getLanguageTexts: () => {
    return Promise.resolve({ testKey: 'Test Text' })
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
  it('substitutes internationalized text', async () => {
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
