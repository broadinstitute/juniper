import React from 'react'
import { act, render, screen } from '@testing-library/react'

import { StudyEnvConfigView } from './StudySettings'
import { mockPortalContext, mockStudyEnvContext } from 'test-utils/mocking-utils'
import { MockSuperuserProvider } from 'test-utils/user-mocking-utils'
import { userEvent } from '@testing-library/user-event'
import { EnvironmentName } from '@juniper/ui-core'

jest.mock('api/api', () => ({
  ...jest.requireActual('api/api'),
  fetchAllowedKitTypes: jest.fn().mockResolvedValue([]),
  fetchKitTypes: jest.fn().mockResolvedValue([])
}))

test('renders a study env. config', async () => {
  const portalContext = mockPortalContext()
  const studyEnvContext = mockStudyEnvContext()
  const expectedConfig = studyEnvContext.currentEnv.studyEnvironmentConfig

  await act(async () => {
    render(
      <MockSuperuserProvider>
        <StudyEnvConfigView portalContext={portalContext} studyEnvContext={studyEnvContext}/>
      </MockSuperuserProvider>
    )
  })

  expect(screen.getByLabelText('password')).toHaveValue(expectedConfig.password)
  expect((screen.getByLabelText('password protected') as HTMLInputElement).checked)
    .toBe(expectedConfig.passwordProtected)
})

test('updates display when study env. changes', async () => {
  const portalContext = mockPortalContext()
  const studyEnvContext = mockStudyEnvContext()
  const expectedConfig = studyEnvContext.currentEnv.studyEnvironmentConfig
  const { rerender } = render(<MockSuperuserProvider>
    <StudyEnvConfigView portalContext={portalContext} studyEnvContext={studyEnvContext}/>
  </MockSuperuserProvider>)
  expect(screen.getByLabelText('password')).toHaveValue(expectedConfig.password)
  const differentEnvContext = {
    ...studyEnvContext,
    currentEnv: {
      ...studyEnvContext.currentEnv,
      studyEnvironmentConfig: {
        ...studyEnvContext.currentEnv.studyEnvironmentConfig,
        password: 'newPass3'
      },
      environmentName: 'irb' as EnvironmentName
    }
  }
  rerender(<MockSuperuserProvider>
    <StudyEnvConfigView portalContext={portalContext} studyEnvContext={differentEnvContext}/>
  </MockSuperuserProvider>)
  expect(screen.getByLabelText('password')).toHaveValue('newPass3')
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
