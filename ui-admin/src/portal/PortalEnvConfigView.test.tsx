import React from 'react'
import { render, screen, fireEvent } from '@testing-library/react'

import PortalEnvConfigView from './PortalEnvConfigView'
import { mockPortalContext } from 'test-utils/mocking-utils'
import { MockRegularUserProvider, MockSuperuserProvider } from 'test-utils/user-mocking-utils'
import { Portal, PortalEnvironment } from '@juniper/ui-core/build/types/portal'

test('renders a portal env. config', async () => {
  const portalContext = mockPortalContext()
  const portalEnv = portalContext.portal?.portalEnvironments[0] as PortalEnvironment
  const envConfig = portalEnv.portalEnvironmentConfig
  render(
    <MockSuperuserProvider>
      <PortalEnvConfigView portalContext={portalContext} portalEnv={portalEnv}/>
    </MockSuperuserProvider>)

  expect(screen.getByLabelText('password')).toHaveValue(envConfig.password)
  expect((screen.getByLabelText('password protected') as HTMLInputElement).checked).toBe(envConfig.passwordProtected)
  expect((screen.getByLabelText('accepting registration') as HTMLInputElement).checked)
    .toBe(envConfig.acceptingRegistration)
})

test('updates a portal env. config', async () => {
  const portalContext = mockPortalContext()
  const portalEnv = portalContext.portal?.portalEnvironments[0] as PortalEnvironment
  render(<MockSuperuserProvider>
    <PortalEnvConfigView portalContext={portalContext} portalEnv={portalEnv}/>
  </MockSuperuserProvider>)
  fireEvent.change(screen.getByLabelText('password'), { target: { value: 'newPass' } })
  expect(screen.getByLabelText('password')).toHaveValue('newPass')
  expect(screen.getByText('Save')).toHaveAttribute('aria-disabled', 'false')
})

test('save disabled for non-superusers', async () => {
  const portalContext = mockPortalContext()
  const portalEnv = portalContext.portal?.portalEnvironments[0] as PortalEnvironment
  render(
    <MockRegularUserProvider>
      <PortalEnvConfigView portalContext={portalContext} portalEnv={portalEnv}/>
    </MockRegularUserProvider>)

  expect(screen.getByText('Save')).toHaveAttribute('aria-disabled', 'true')
})
