import React from 'react'
import { render, screen, fireEvent } from '@testing-library/react'

import PortalEnvConfigView from './PortalEnvConfigView'
import { mockPortalContext, MockRegularUserProvider, MockSuperuserProvider } from 'test-utils/mocking-utils'
import { Portal, PortalEnvironment } from '@juniper/ui-core/build/types/portal'

test('renders a portal env. config', async () => {
  const portalContext = mockPortalContext()
  const portalEnv = portalContext.portal?.portalEnvironments[0] as PortalEnvironment
  const envConfig = portalEnv.portalEnvironmentConfig
  render(
    <MockSuperuserProvider>
      <PortalEnvConfigView portal={portalContext.portal as Portal}
        portalEnv={portalEnv} updatePortal={portalContext.updatePortal}/>
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
    <PortalEnvConfigView portal={portalContext.portal as Portal}
      portalEnv={portalEnv} updatePortal={portalContext.updatePortal}/>
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
      <PortalEnvConfigView portal={portalContext.portal as Portal}
        portalEnv={portalEnv} updatePortal={portalContext.updatePortal}/>
    </MockRegularUserProvider>)

  expect(screen.getByText('Save')).toHaveAttribute('aria-disabled', 'true')
})
