import React from 'react'

import { setupRouterTest } from 'test-utils/router-testing-utils'
import { mockAdminUser, MockUserProvider } from 'test-utils/user-mocking-utils'
import { render, screen } from '@testing-library/react'
import AdminNavbar from './AdminNavbar'
import userEvent from '@testing-library/user-event'
import { emptyNavbarContext } from './NavbarProvider'

test('renders the help menu', async () => {
  const { RoutedComponent } = setupRouterTest(
    <MockUserProvider user={mockAdminUser(false)}>
      <AdminNavbar {...emptyNavbarContext}/>
    </MockUserProvider>)
  render(RoutedComponent)
  expect(screen.getByTitle('help menu')).toBeInTheDocument()
  userEvent.click(screen.getByTitle('help menu'))
  expect(screen.getByText('Contact support')).toBeInTheDocument()
})

test('renders the user menu', async () => {
  const { RoutedComponent } = setupRouterTest(
    <MockUserProvider user={{
      ...mockAdminUser(false),
      username: 'testuser123'
    }}>
      <AdminNavbar {...emptyNavbarContext}/>
    </MockUserProvider>)
  render(RoutedComponent)
  expect(screen.getByTitle('user menu')).toBeInTheDocument()
  userEvent.click(screen.getByTitle('user menu'))
  expect(screen.getByText('testuser123')).toBeInTheDocument()
})
