import React from 'react'
import { render, waitFor } from '@testing-library/react'
import { useI18n } from './I18nProvider'
import { MockI18nProvider } from 'src/participant/i18n-testing-utils'

describe('I18nProvider', () => {
  it('substitutes internationalized text', async () => {
    const TestComponent = () => {
      const { i18n } = useI18n()
      return <span>{i18n('testKey')}</span>
    }

    const { getByText } = render(
      <MockI18nProvider mockTexts={{ testKey: 'Test Text' }}>
        <TestComponent />
      </MockI18nProvider>
    )

    await waitFor(() => {
      expect(getByText('Test Text')).toBeInTheDocument()
    })
  })
})
