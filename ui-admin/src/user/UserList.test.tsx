import React from 'react'
import { renderWithRouter, setupRouterTest } from 'test-utils/router-testing-utils'
import { render, screen, waitFor } from '@testing-library/react'
import { mockAdminUser } from '../test-utils/user-mocking-utils'
import UserList from './UserList'
import Api from '../api/api'
import { mockPortal } from '../test-utils/mocking-utils'
import { expectCellToHaveText, getTableCell } from '../test-utils/table-testing-utils'

describe('UserList', () => {
  test('shows a list of all users', async () => {
    jest.spyOn(Api, 'fetchAdminUsers').mockResolvedValue([
      {
        ...mockAdminUser(true),
        username: 'superuser@test.com'
      }, {
        ...mockAdminUser(false),
        username: 'staff@test.com',
        portalAdminUsers: [{
          portalId: 'portal2',
          roles: []
        }]
      }
    ])
    jest.spyOn(Api, 'getPortals').mockResolvedValue([{
      ...mockPortal(),
      shortcode: 'portal1'
    }, {
      ...mockPortal(),
      id: 'portal2',
      shortcode: 'portal2'
    }])

    renderWithRouter(<UserList />)

    await waitFor(() => expect(screen.queryByTestId('loading-spinner')).not.toBeInTheDocument())

    // superuser check shows correctly
    const userTable = screen.getByRole('table')
    const superuserCell = getTableCell(userTable as HTMLTableElement, 'superuser@test.com', 'Superuser')
    expect(superuserCell!.firstElementChild!.getAttribute('aria-label')).toEqual('yes')

    const staffsuperuserCell = getTableCell(userTable as HTMLTableElement, 'staff@test.com', 'Superuser')
    expect(staffsuperuserCell.firstElementChild).toEqual(null)

    // Portal list shows correctly
    expectCellToHaveText(userTable as HTMLTableElement, 'staff@test.com', 'Portals', 'portal2')
  })

  test('shows a list of portal-specific users', async () => {
    const portal2 = {
      ...mockPortal(),
      id: 'portal2',
      shortcode: 'portal2'
    }
    jest.spyOn(Api, 'fetchAdminUsersByPortal').mockResolvedValue([
      {
        ...mockAdminUser(false),
        username: 'staff@test.com',
        portalAdminUsers: [{
          portalId: 'portal2',
          roles: []
        }]
      }
    ])
    jest.spyOn(Api, 'getPortals').mockResolvedValue([portal2])

    renderWithRouter(<UserList portal={portal2}/>)

    await waitFor(() => expect(screen.queryByTestId('loading-spinner')).not.toBeInTheDocument())
    expect(screen.getByText('staff@test.com')).toBeInTheDocument()
    // for portal-specific lists, we don't show superuser or portal columns
    expect(screen.queryByText('Portals')).not.toBeInTheDocument()
    expect(screen.queryByText('Superuser')).not.toBeInTheDocument()
  })
})
