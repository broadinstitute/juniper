import React from 'react'
import Api from 'api/api'
import { render, screen } from '@testing-library/react'
import IntegrationDashboard from './IntegrationDashboard'
import { userEvent } from '@testing-library/user-event'
import { setupRouterTest } from '@juniper/ui-core'

test('routes to integration paths', async () => {
  jest.spyOn(Api, 'fetchInternalConfig').mockResolvedValue({
    pepperDsmConfig: {},
    addrValidationConfig: {}
  })
  const { RoutedComponent } = setupRouterTest(<IntegrationDashboard/>)
  render(RoutedComponent)
  expect(screen.queryByText('basePath')).not.toBeInTheDocument()
  await userEvent.click(screen.getByText('Kits'))

  expect(screen.queryByText('basePath')).toBeInTheDocument()
})
