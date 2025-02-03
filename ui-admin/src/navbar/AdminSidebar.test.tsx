import React from 'react'

import { mockAdminUser, MockUserProvider } from 'test-utils/user-mocking-utils'
import { render, screen, waitFor } from '@testing-library/react'
import AdminSidebar from './AdminSidebar'
import { userEvent } from '@testing-library/user-event'
import { Config } from '../api/api'
import { setupRouterTest } from '@juniper/ui-core'


const testConfig: Config = {
  b2cTenantName: '',
  adminApiHostname: '',
  adminUiHostname: '',
  b2cClientId: '',
  b2cPolicyName: '',
  participantApiHostname: '',
  participantUiHostname: '',
  deploymentZone: 'live',
  systemSettings: {
    maintenanceModeEnabled: false,
    maintenanceModeMessage: '',
    maintenanceModeBypassPhrase: '',
    disableScheduledJobs: false
  }
}
test('renders the superuser menu for superusers', async () => {
  const { RoutedComponent } = setupRouterTest(
    <MockUserProvider user={mockAdminUser(true)}>
      <AdminSidebar config={testConfig}/>
    </MockUserProvider>)
  render(RoutedComponent)
  expect(screen.getByText('Superuser Functions')).toBeInTheDocument()
})

test('menu components collapse on click', async () => {
  const { RoutedComponent } = setupRouterTest(
    <MockUserProvider user={mockAdminUser(true)}>
      <AdminSidebar config={testConfig}/>
    </MockUserProvider>)
  render(RoutedComponent)
  expect(screen.getByText('All Users')).toBeVisible()
  await userEvent.click(screen.getByText('Superuser Functions'))
  waitFor(() => expect(screen.queryByText('All Users')).not.toBeVisible())
  await userEvent.click(screen.getByText('Superuser Functions'))
  waitFor(() => expect(screen.queryByText('All Users')).toBeVisible())
})

test('does not render the superuser menu for  regular users', async () => {
  const { RoutedComponent } = setupRouterTest(
    <MockUserProvider user={mockAdminUser(false)}>
      <AdminSidebar config={testConfig}/>
    </MockUserProvider>)
  render(RoutedComponent)
  expect(screen.queryByText('Superuser Functions')).toBeNull()
})

