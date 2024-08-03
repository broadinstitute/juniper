import React from 'react'
import {
  render,
  screen,
  waitFor
} from '@testing-library/react'

import Api, { MailingListContact } from 'api/api'
import { mockPortalContext } from 'test-utils/mocking-utils'
import { userEvent } from '@testing-library/user-event'
import MailingListView from './MailingListView'
import {
  MockI18nProvider,
  setupRouterTest
} from '@juniper/ui-core'

const contacts: MailingListContact[] = [{
  id: 'id1',
  name: 'person1',
  email: 'fake1@test.com',
  createdAt: 0
}, {
  id: 'id2',
  name: 'person2',
  email: 'fake2@test.com',
  createdAt: 1
}]

test('renders a mailing list', async () => {
  // avoid cluttering the console with the info messages from the table creation
  jest.spyOn(console, 'info').mockImplementation(jest.fn())
  jest.spyOn(Api, 'fetchMailingList').mockResolvedValue(contacts)
  const portalContext = mockPortalContext()
  const portalEnv = portalContext.portal.portalEnvironments[0]
  const { RoutedComponent } =
    setupRouterTest(<MockI18nProvider>
      <MailingListView portalContext={portalContext}
        portalEnv={portalEnv}/>
    </MockI18nProvider>)
  render(RoutedComponent)
  await waitFor(() => {
    expect(screen.getByText('person1')).toBeInTheDocument()
  })
  expect(screen.getByText('fake1@test.com')).toBeInTheDocument()
  expect(screen.getByText('person2')).toBeInTheDocument()
})

test('renders a download mailing list button', async () => {
  jest.spyOn(Api, 'fetchMailingList').mockResolvedValue(contacts)
  const portalContext = mockPortalContext()
  const portalEnv = portalContext.portal.portalEnvironments[0]
  const { RoutedComponent } =
    setupRouterTest(<MockI18nProvider>
      <MailingListView portalContext={portalContext} portalEnv={portalEnv}/>
    </MockI18nProvider>)
  render(RoutedComponent)
  await waitFor(() => {
    expect(screen.getByText('person1')).toBeInTheDocument()
  })
  const downloadButton = screen.getByText('Download')
  expect(downloadButton).toBeInTheDocument()

  await userEvent.click(downloadButton)
  await waitFor(() => {
    expect(screen.getByText('person1')).toBeInTheDocument()
  })
})

test('delete button shows confirmation', async () => {
  jest.spyOn(Api, 'fetchMailingList').mockResolvedValue(contacts)
  const portalContext = mockPortalContext()
  const portalEnv = portalContext.portal.portalEnvironments[0]
  const { RoutedComponent } =
    setupRouterTest(<MockI18nProvider>
      <MailingListView portalContext={portalContext} portalEnv={portalEnv}/>
    </MockI18nProvider>)
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

test('sorts by join date by default', async () => {
  jest.spyOn(Api, 'fetchMailingList').mockResolvedValue(contacts)
  const portalContext = mockPortalContext()
  const portalEnv = portalContext.portal.portalEnvironments[0]
  const { RoutedComponent } =
    setupRouterTest(<MockI18nProvider>
      <MailingListView portalContext={portalContext} portalEnv={portalEnv}/>
    </MockI18nProvider>)
  render(RoutedComponent)
  await waitFor(() => {
    expect(screen.getByText('person1')).toBeInTheDocument()
  })

  const joined = screen.getByText('Joined')


  // "Joined" text is two parents away from the <th> element
  expect(joined.closest('[aria-sort]')?.getAttribute('aria-sort')).toBe('descending')
})
