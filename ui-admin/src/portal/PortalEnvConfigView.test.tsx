import React from 'react'
import { render, screen } from '@testing-library/react'

import PortalEnvConfigView from './PortalEnvConfigView'
import { mockPortalContext } from 'test-utils/mocking-utils'
import { MockRegularUserProvider, MockSuperuserProvider } from 'test-utils/user-mocking-utils'
import { PortalEnvironment } from '@juniper/ui-core'
import { userEvent } from '@testing-library/user-event'

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

test('changes state when portal env changes', async () => {
  const portalContext = mockPortalContext()
  const portalEnv = portalContext.portal?.portalEnvironments[0] as PortalEnvironment
  const envConfig = portalEnv.portalEnvironmentConfig
  const { rerender } = render(<MockSuperuserProvider>
    <PortalEnvConfigView portalContext={portalContext} portalEnv={portalEnv}/>
  </MockSuperuserProvider>)

  expect(screen.getByLabelText('password')).toHaveValue(envConfig.password)
  const differentEnv = {
    ...portalEnv,
    portalEnvironmentConfig: {
      ...portalEnv.portalEnvironmentConfig,
      password: 'newPass3'
    },
    environmentName: 'irb'
  }
  rerender(<MockSuperuserProvider>
    <PortalEnvConfigView portalContext={portalContext} portalEnv={differentEnv}/>
  </MockSuperuserProvider>)
  expect(screen.getByLabelText('password')).toHaveValue('newPass3')
})

test('updates a portal env. config', async () => {
  const portalContext = mockPortalContext()
  const portalEnv = portalContext.portal?.portalEnvironments[0] as PortalEnvironment
  render(<MockSuperuserProvider>
    <PortalEnvConfigView portalContext={portalContext} portalEnv={portalEnv}/>
  </MockSuperuserProvider>)
  const input = screen.getByLabelText('password') as HTMLInputElement
  // select all:
  await userEvent.clear(input)
  await userEvent.type(input, 'newPass')
  expect(input).toHaveValue('newPass')
  expect(screen.getByText('Save website config')).toHaveAttribute('aria-disabled', 'false')
})

test('save disabled for non-superusers', async () => {
  const portalContext = mockPortalContext()
  const portalEnv = portalContext.portal?.portalEnvironments[0] as PortalEnvironment
  render(
    <MockRegularUserProvider>
      <PortalEnvConfigView portalContext={portalContext} portalEnv={portalEnv}/>
    </MockRegularUserProvider>)

  expect(screen.getByText('Save website config')).toHaveAttribute('aria-disabled', 'true')
})
