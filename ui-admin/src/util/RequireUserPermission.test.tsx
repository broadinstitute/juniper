import {
  Portal,
  setupRouterTest
} from '@juniper/ui-core'
import {
  render,
  screen
} from '@testing-library/react'
import React from 'react'
import { RequireUserPermission } from 'util/RequireUserPermission'
import {
  mockAdminUser,
  mockAdminUserWithPerms,
  MockUserProvider
} from 'test-utils/user-mocking-utils'


describe('RequireUserPermission', () => {
  test('allows superusers', () => {
    const { RoutedComponent } = setupRouterTest(
      <MockUserProvider user={mockAdminUser(true)}>
        <RequireUserPermission
          superuser
        >
          secret content
        </RequireUserPermission>
      </MockUserProvider>)

    render(RoutedComponent)

    expect(screen.getByText('secret content')).toBeInTheDocument()
  })
  test('does not allow non superusers', () => {
    const { RoutedComponent } = setupRouterTest(
      <MockUserProvider user={mockAdminUser(false)}>
        <RequireUserPermission
          superuser
        >
          secret content
        </RequireUserPermission>
      </MockUserProvider>)

    render(RoutedComponent)

    expect(screen.queryByText('secret content')).not.toBeInTheDocument()
  })
  test('allows correct perms', () => {
    const user = mockAdminUserWithPerms('portal1', ['perm1'])

    const { RoutedComponent } = setupRouterTest(
      <MockUserProvider user={user}>
        <RequireUserPermission
          portal={{ id: 'portal1' } as Portal}
          perms={['perm1']}
        >
          secret content
        </RequireUserPermission>
      </MockUserProvider>)

    render(RoutedComponent)

    expect(screen.getByText('secret content')).toBeInTheDocument()
  })
  it('does not allow incorrect portal', () => {
    const user = mockAdminUserWithPerms('portal1', ['perm1'])

    const { RoutedComponent } = setupRouterTest(
      <MockUserProvider user={user}>
        <RequireUserPermission
          portal={{ id: 'portal2' } as Portal}
          perms={['perm1']}
        >
          secret content
        </RequireUserPermission>
      </MockUserProvider>)

    render(RoutedComponent)

    expect(screen.queryByText('secret content')).not.toBeInTheDocument()
  })
  test('does not allow incorrect perms', () => {
    const user = mockAdminUserWithPerms('portal1', ['perm1'])

    const { RoutedComponent } = setupRouterTest(
      <MockUserProvider user={user}>
        <RequireUserPermission
          portal={{ id: 'portal1' } as Portal}
          perms={['perm2']}
        >
          secret content
        </RequireUserPermission>
      </MockUserProvider>)

    render(RoutedComponent)

    expect(screen.queryByText('secret content')).not.toBeInTheDocument()
  })
  test('requires multiple perms', () => {
    const user = mockAdminUserWithPerms('portal1', ['perm2'])

    const { RoutedComponent } = setupRouterTest(
      <MockUserProvider user={user}>
        <RequireUserPermission
          portal={{ id: 'portal1' } as Portal}
          perms={['perm1', 'perm2']}
        >
          secret content
        </RequireUserPermission>
      </MockUserProvider>)

    render(RoutedComponent)

    expect(screen.queryByText('secret content')).not.toBeInTheDocument()
  })
})
