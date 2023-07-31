import React from 'react'

import { InitializedSiteContentView } from './SiteContentView'
import { setupRouterTest } from 'test-utils/router-testing-utils'
import { render, screen, waitFor } from '@testing-library/react'
import { emptyApi, mockSiteContent } from 'test-utils/mock-site-content'
import userEvent from '@testing-library/user-event'

test('enables live-preview text editing', async () => {
  const siteContent = mockSiteContent()
  const { RoutedComponent } = setupRouterTest(
    <InitializedSiteContentView siteContent={siteContent} previewApi={emptyApi}/>)
  render(RoutedComponent)

  expect(screen.getByText('Landing page')).toBeInTheDocument()

  const sectionInput = screen.getByRole('textbox')
  expect(screen.getByRole('heading').textContent).toEqual('about us')
  userEvent.pointer({ target: sectionInput, offset: 22, keys: '[MouseLeft]' })
  userEvent.keyboard('!!')

  await waitFor(() => expect(screen.getByRole('heading')).toHaveTextContent('about us!!'))
})
