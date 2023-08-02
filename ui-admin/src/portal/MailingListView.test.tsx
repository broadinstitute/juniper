import React from 'react'
import { render, screen, waitFor } from '@testing-library/react'

import { MailingListContact } from 'api/api'
import { mockPortalContext } from 'test-utils/mocking-utils'
import { setupRouterTest } from 'test-utils/router-testing-utils'
import userEvent from '@testing-library/user-event'
import MailingListView from './MailingListView'

jest.mock('api/api', () => ({
  fetchMailingList: () => {
    const contacts: MailingListContact[] = [{
      name: 'person1',
      email: 'fake1@test.com',
      createdAt: 0
    }, {
      name: 'person2',
      email: 'fake2@test.com',
      createdAt: 0
    }]
    return Promise.resolve(contacts)
  }
}))

test('renders a mailing list', async () => {
  const portalContext = mockPortalContext()
  const portalEnv = portalContext.portal.portalEnvironments[0]
  const { RoutedComponent } =
        setupRouterTest(<MailingListView portalContext={portalContext} portalEnv={portalEnv}/>)
  render(RoutedComponent)
  await waitFor(() => {
    expect(screen.getByText('person1')).toBeInTheDocument()
  })
  expect(screen.getByText('fake1@test.com')).toBeInTheDocument()
  expect(screen.getByText('person2')).toBeInTheDocument()
})

test('download is toggled depending on contacts selected', async () => {
  const portalContext = mockPortalContext()
  const portalEnv = portalContext.portal.portalEnvironments[0]
  const { RoutedComponent } =
        setupRouterTest(<MailingListView portalContext={portalContext} portalEnv={portalEnv}/>)
  render(RoutedComponent)
  await waitFor(() => {
    expect(screen.getByText('person1')).toBeInTheDocument()
  })
  const downloadLink = screen.getByText('Download')
  expect(downloadLink).toHaveAttribute('aria-disabled', 'true')

  // click on the 'select all' checkbox
  await userEvent.click(screen.getAllByRole('checkbox')[0])
  expect(screen.getByText('2 of 2 selected')).toBeInTheDocument()
  expect(downloadLink).toHaveAttribute('aria-disabled', 'false')
})
