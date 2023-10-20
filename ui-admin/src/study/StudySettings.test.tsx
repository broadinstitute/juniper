import React from 'react'
import { render, screen } from '@testing-library/react'

import { StudyEnvConfigView } from './StudySettings'
import { mockPortalContext, mockStudyEnvContext } from 'test-utils/mocking-utils'
import { MockRegularUserProvider, MockSuperuserProvider } from 'test-utils/user-mocking-utils'
import userEvent from '@testing-library/user-event'

test('renders a study env. config', async () => {
  const portalContext = mockPortalContext()
  const studyEnvContext = mockStudyEnvContext()
  const expectedConfig = studyEnvContext.currentEnv.studyEnvironmentConfig
  render(
    <MockSuperuserProvider>
      <StudyEnvConfigView portalContext={portalContext} studyEnvContext={studyEnvContext}/>
    </MockSuperuserProvider>)

  expect(screen.getByLabelText('password')).toHaveValue(expectedConfig.password)
  expect((screen.getByLabelText('password protected') as HTMLInputElement).checked)
    .toBe(expectedConfig.passwordProtected)
})

test('updates a study env. config', async () => {
  const portalContext = mockPortalContext()
  const studyEnvContext = mockStudyEnvContext()
  render(
    <MockSuperuserProvider>
      <StudyEnvConfigView portalContext={portalContext} studyEnvContext={studyEnvContext}/>
    </MockSuperuserProvider>)
  const input = screen.getByLabelText('password') as HTMLInputElement
  // select all:
  await userEvent.clear(input)
  await userEvent.type(input, 'newPass')
  expect(input).toHaveValue('newPass')
  expect(screen.getByText('Save study config')).toHaveAttribute('aria-disabled', 'false')
})

test('save disabled for non-superusers', async () => {
  const portalContext = mockPortalContext()
  const studyEnvContext = mockStudyEnvContext()
  render(
    <MockRegularUserProvider>
      <StudyEnvConfigView portalContext={portalContext} studyEnvContext={studyEnvContext}/>
    </MockRegularUserProvider>)
  expect(screen.getByText('Save study config')).toHaveAttribute('aria-disabled', 'true')
})
