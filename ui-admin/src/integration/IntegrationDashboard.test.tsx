import React from 'react'
import Api from 'api/api'
import { render, screen } from '@testing-library/react'
import IntegrationDashboard from './IntegrationDashboard'
import { setupRouterTest } from '../test-utils/router-testing-utils'
import userEvent from '@testing-library/user-event'

test('routes to integration paths', async () => {
  jest.spyOn(Api, 'fetchInternalConfig').mockResolvedValue({
    pepperDsmConfig: {},
    addrValidationConfig: {}
  })
  const { RoutedComponent } = setupRouterTest(<IntegrationDashboard/>)
  render(RoutedComponent)
  expect(screen.queryByText('useLiveDsm')).not.toBeInTheDocument()
  await userEvent.click(screen.getByText('Kits'))

  expect(screen.queryByText('useLiveDsm')).toBeInTheDocument()
})
