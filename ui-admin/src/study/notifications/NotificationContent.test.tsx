import React from 'react'
import { render, screen, waitFor } from '@testing-library/react'

import { mockNotificationConfig, mockPortalContext, mockStudyEnvContext } from 'test-utils/mocking-utils'
import { setupRouterTest } from 'test-utils/router-testing-utils'
import NotificationContent from './NotificationContent'
import userEvent from '@testing-library/user-event'
import Api from 'api/api'
import { ReactNotifications } from 'react-notifications-component'

test('renders routable config list', async () => {
  const studyEnvContext = mockStudyEnvContext()
  const enrollEmailConfig = {
    ...mockNotificationConfig(),
    id: 'event1',
    notificationType: 'EVENT',
    eventType: 'STUDY_ENROLLMENT'
  }
  const notificationConfigs = [
    enrollEmailConfig,
    {
      ...mockNotificationConfig(),
      id: 'reminder1',
      notificationType: 'TASK_REMINDER',
      taskType: 'CONSENT'
    },
    {
      ...mockNotificationConfig(),
      id: 'reminder2',
      notificationType: 'TASK_REMINDER',
      taskType: 'SURVEY'
    }
  ]
  jest.spyOn(Api, 'findNotificationConfigsForStudyEnv')
    .mockImplementation(() => Promise.resolve(notificationConfigs))

  const { RoutedComponent, router } =
      setupRouterTest(<>
        <ReactNotifications/>
        <NotificationContent studyEnvContext={studyEnvContext} portalContext={mockPortalContext()}/>
      </>)
  render(RoutedComponent)
  expect(screen.getByText('Participant email configuration')).toBeInTheDocument()
  await waitFor(() => expect(screen.getByText('Study enrollment')).toBeInTheDocument())
  expect(screen.getByText('Reminder: CONSENT')).toBeInTheDocument()
  expect(screen.getByText('Reminder: SURVEY')).toBeInTheDocument()

  await userEvent.click(screen.getByText('Study enrollment'))
  expect(router.state.location.pathname).toEqual(`/configs/${enrollEmailConfig.id}`)
})
