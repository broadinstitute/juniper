import React from 'react'
import {
  screen,
  waitFor
} from '@testing-library/react'

import Api from 'api/api'
import { mockPortal, renderInPortalRouter } from 'test-utils/mocking-utils'

import PortalChangeHistoryView from './PortalChangeHistoryView'
import { mockAdminUser } from 'test-utils/user-mocking-utils'

test('renders a list of changes', async () => {
  // avoid cluttering the console with the info messages from the table creation
  jest.spyOn(Api, 'fetchPortalEnvChangeRecords').mockResolvedValue([{
    adminUserId: 'admin1',
    createdAt: 0,
    portalId: 'portal1',
    portalEnvironmentChange: '{"key": "value"}'
  }])
  const portal = mockPortal()
  renderInPortalRouter(portal, <PortalChangeHistoryView portal={portal}/>, {
    adminUsers: [{
      ...mockAdminUser(false), id: 'admin1', username: 'staffPerson'
    }]
  })
  await waitFor(() => {
    expect(screen.queryByTestId('loading-spinner')).not.toBeInTheDocument()
  })
  expect(screen.getByText('staffPerson')).toBeInTheDocument()
})
