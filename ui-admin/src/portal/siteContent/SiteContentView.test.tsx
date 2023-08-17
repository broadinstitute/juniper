import React from 'react'

import { InitializedSiteContentView } from './SiteContentView'
import { setupRouterTest } from 'test-utils/router-testing-utils'
import { render, screen, waitFor } from '@testing-library/react'
import { emptyApi, mockSiteContent } from 'test-utils/mock-site-content'
import userEvent from '@testing-library/user-event'

test('enables live-preview text editing', async () => {
  const siteContent = mockSiteContent()
  const { RoutedComponent } = setupRouterTest(
    <InitializedSiteContentView siteContent={siteContent} previewApi={emptyApi}
      loadSiteContent={() => 1}
      setSiteContent={() => 1} portalShortcode="foo"/>)
  render(RoutedComponent)

  expect(screen.getByText('Landing page')).toBeInTheDocument()

  const sectionInput = screen.getByRole('textbox')
  const aboutUsHeading = screen.queryAllByRole('heading')
      .find(el => el.textContent === 'about us')
  expect(aboutUsHeading).toBeInTheDocument()
  userEvent.pointer({ target: sectionInput, offset: 22, keys: '[MouseLeft]' })
  userEvent.keyboard('!!')

  await waitFor(() => {
    const aboutUsNewHeading = screen.queryAllByRole('heading')
        .find(el => el.textContent === 'about us!!')
    return expect(aboutUsNewHeading).toBeInTheDocument()
  })
})
