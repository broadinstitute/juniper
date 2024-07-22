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
  deploymentZone: 'live'
}
test('renders the superuser menu for superusers', async () => {
  const { RoutedComponent } = setupRouterTest(
    <MockUserProvider user={mockAdminUser(true)}>
      <AdminSidebar config={testConfig}/>
    </MockUserProvider>)
  render(RoutedComponent)
  expect(screen.getByText('Superuser functions')).toBeInTheDocument()
})

test('menu components collapse on click', async () => {
  const { RoutedComponent } = setupRouterTest(
    <MockUserProvider user={mockAdminUser(true)}>
      <AdminSidebar config={testConfig}/>
    </MockUserProvider>)
  render(RoutedComponent)
  expect(screen.getByText('All users')).toBeVisible()
  await userEvent.click(screen.getByText('Superuser functions'))
  waitFor(() => expect(screen.queryByText('All users')).not.toBeVisible())
  await userEvent.click(screen.getByText('Superuser functions'))
  waitFor(() => expect(screen.queryByText('All users')).toBeVisible())
})

test('does not render the superuser menu for  regular users', async () => {
  const { RoutedComponent } = setupRouterTest(
    <MockUserProvider user={mockAdminUser(false)}>
      <AdminSidebar config={testConfig}/>
    </MockUserProvider>)
  render(RoutedComponent)
  expect(screen.queryByText('Superuser functions')).toBeNull()
})

