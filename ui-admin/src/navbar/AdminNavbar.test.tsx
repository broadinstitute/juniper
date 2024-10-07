import React from 'react'

import { mockAdminUser, MockUserProvider } from 'test-utils/user-mocking-utils'
import { render, screen } from '@testing-library/react'
import AdminNavbar from './AdminNavbar'
import { userEvent } from '@testing-library/user-event'
import { setupRouterTest } from '@juniper/ui-core'

test('renders the help menu', async () => {
  const { RoutedComponent } = setupRouterTest(
    <MockUserProvider user={mockAdminUser(false)}>
      <AdminNavbar/>
    </MockUserProvider>)
  render(RoutedComponent)
  expect(screen.getByTitle('help menu')).toBeInTheDocument()
  userEvent.click(screen.getByTitle('help menu'))
  expect(screen.getByText('Contact support')).toBeInTheDocument()
  expect(screen.getByText('Help pages')).toHaveAttribute('href', 'https://broad-juniper.zendesk.com')
})

test('renders the user menu', async () => {
  const { RoutedComponent } = setupRouterTest(
    <MockUserProvider user={{
      ...mockAdminUser(false),
      username: 'testuser123'
    }}>
      <AdminNavbar/>
    </MockUserProvider>)
  render(RoutedComponent)
  expect(screen.getByTitle('user menu')).toBeInTheDocument()
  userEvent.click(screen.getByTitle('user menu'))
  expect(screen.queryAllByText('testuser123')).toHaveLength(2)
})
