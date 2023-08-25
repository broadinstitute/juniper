import React from 'react'
import { render, screen, waitFor } from '@testing-library/react'

import Api, { MailingListContact } from 'api/api'
import { mockPortalContext } from 'test-utils/mocking-utils'
import { setupRouterTest } from 'test-utils/router-testing-utils'
import userEvent from '@testing-library/user-event'
import MailingListView from './MailingListView'

const contacts: MailingListContact[] = [{
  id: 'id1',
  name: 'person1',
  email: 'fake1@test.com',
  createdAt: 0
}, {
  id: 'id2',
  name: 'person2',
  email: 'fake2@test.com',
  createdAt: 0
}]

test('renders a mailing list', async () => {
  jest.spyOn(Api, 'fetchMailingList').mockImplementation(() => Promise.resolve(contacts))
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
  jest.spyOn(Api, 'fetchMailingList').mockImplementation(() => Promise.resolve(contacts))
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


test('delete button shows confirmation', async () => {
  jest.spyOn(Api, 'fetchMailingList').mockImplementation(() => Promise.resolve(contacts))
  const portalContext = mockPortalContext()
  const portalEnv = portalContext.portal.portalEnvironments[0]
  const { RoutedComponent } =
      setupRouterTest(<MailingListView portalContext={portalContext} portalEnv={portalEnv}/>)
  render(RoutedComponent)
  await waitFor(() => {
    expect(screen.getByText('person1')).toBeInTheDocument()
  })
  expect(screen.getByText('Remove')).toHaveAttribute('aria-disabled', 'true')


  const selectAll = screen.getAllByRole('checkbox')[0]
  await userEvent.click(selectAll)
  await userEvent.click(screen.getByText('Remove'))
  expect(screen.getByText('This operation CANNOT BE UNDONE.')).toBeInTheDocument()
})
