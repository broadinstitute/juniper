import React from 'react'
import EmailTemplateEditor from './EmailTemplateEditor'
import { screen, render } from '@testing-library/react'
import { mockEmailTemplate } from 'test-utils/mocking-utils'
import { asMockedFn } from '@juniper/ui-core'
import { usePortalLanguage } from 'portal/useDefaultPortalLanguage'

jest.mock('portal/useDefaultPortalLanguage', () => ({
  usePortalLanguage: jest.fn()
}))

describe('EmailTemplateEditor', () => {
  it('should render a language dropdown when there are multiple languages', () => {
    asMockedFn(usePortalLanguage).mockReturnValue({
      defaultLanguage: {
        languageCode: 'en',
        languageName: 'English'
      },
      supportedLanguages: [{
        languageCode: 'en',
        languageName: 'English'
      }, {
        languageCode: 'es',
        languageName: 'Español'
      }]
    })

    render(
      <EmailTemplateEditor
        emailTemplate={mockEmailTemplate()}
        portalShortcode={'foo'}
        updateEmailTemplate={() => jest.fn()}
      />
    )

    expect(screen.getByLabelText('Select a language')).toBeInTheDocument()
  })

  it('should not render a language dropdown when there is only one language', () => {
    asMockedFn(usePortalLanguage).mockReturnValue({
      defaultLanguage: {
        languageCode: 'en',
        languageName: 'English'
      },
      supportedLanguages: [{
        languageCode: 'en',
        languageName: 'English'
      }]
    })

    render(
      <EmailTemplateEditor
        emailTemplate={mockEmailTemplate()}
        portalShortcode={'foo'}
        updateEmailTemplate={() => jest.fn()}
      />
    )

    expect(screen.queryByLabelText('Select a language')).not.toBeInTheDocument()
  })
})
