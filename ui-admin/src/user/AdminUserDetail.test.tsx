import React from 'react'
import { renderWithRouter } from 'test-utils/router-testing-utils'
import { screen, waitFor } from '@testing-library/react'
import { mockAdminUser, MockUserProvider } from 'test-utils/user-mocking-utils'
import Api from '../api/api'
import { expectCellToHaveText } from 'test-utils/table-testing-utils'
import { AdminUserDetailRaw } from './AdminUserDetail'

const mockRoleManager = {
  ...mockAdminUser(false),
  username: 'staff@test.com',
  portalPermissions: {
    portal2: ['team_roles_edit', 'survey_edit']
  },
  portalAdminUsers: [{
    portalId: 'portal2',
    roles: [{
      name: 'study_admin',
      displayName: 'Study Admin',
      description: 'Can manage studies',
      permissions: [{
        name: 'survey_edit',
        displayName: 'Edit surveys',
        description: 'make changes to surveys'
      }, {
        name: 'team_roles_edit',
        displayName: 'manage team roles',
        description: 'roles stuff'
      }]
    }]
  }]
}

describe('AdminUserDetail', () => {
  test('shows a user with roles and permissions', async () => {
    jest.spyOn(Api, 'fetchAdminUser').mockResolvedValue(mockRoleManager)
    renderWithRouter(
      <MockUserProvider user={mockRoleManager}>
        <AdminUserDetailRaw portalShortcode='portal2' adminUserId="id1" />
      </MockUserProvider>
    )

    await waitFor(() => expect(screen.queryByTestId('loading-spinner')).not.toBeInTheDocument())

    // shows role descriptions
    const userTable = screen.getByRole('table')
    expectCellToHaveText(userTable as HTMLTableElement, 'Study Admin', 'Description', 'Can manage studies')

    // shows permissions
    expectCellToHaveText(userTable as HTMLTableElement, 'Study Admin', 'Permissions',
      'Edit surveys: make changes to surveysmanage team roles: roles stuff')
  })

  test('roles and permissions not shown for user', async () => {
    jest.spyOn(Api, 'fetchAdminUser').mockResolvedValue(mockRoleManager)
    renderWithRouter(
      <MockUserProvider user={mockAdminUser(false)}>
        <AdminUserDetailRaw portalShortcode='portal2' adminUserId="id1" />
      </MockUserProvider>
    )

    await waitFor(() => expect(screen.queryByTestId('loading-spinner')).not.toBeInTheDocument())

    // does not show role table
    expect(screen.queryByText('Roles')).not.toBeInTheDocument()
  })
})
