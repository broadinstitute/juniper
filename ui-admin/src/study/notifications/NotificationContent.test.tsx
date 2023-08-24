import React from 'react'
import { render, screen } from '@testing-library/react'

import { mockNotificationConfig, mockStudyEnvContext } from 'test-utils/mocking-utils'
import { setupRouterTest } from 'test-utils/router-testing-utils'
import NotificationContent from './NotificationContent'
import userEvent from '@testing-library/user-event'

test('renders routable config list', async () => {
  const studyEnvContext = mockStudyEnvContext()
  const enrollEmailConfig = {
    ...mockNotificationConfig(),
    id: 'event1',
    notificationType: 'EVENT',
    eventType: 'STUDY_ENROLLMENT'
  }
  studyEnvContext.currentEnv.notificationConfigs = [
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

  const { RoutedComponent, router } = setupRouterTest(<NotificationContent studyEnvContext={studyEnvContext}/>)
  render(RoutedComponent)
  expect(screen.getByText('Participant Notifications')).toBeInTheDocument()
  expect(screen.getByText('Study enrollment')).toBeInTheDocument()
  expect(screen.getByText('Reminder: CONSENT')).toBeInTheDocument()
  expect(screen.getByText('Reminder: SURVEY')).toBeInTheDocument()

  await userEvent.click(screen.getByText('Study enrollment'))
  expect(router.state.location.pathname).toEqual(`/configs/${enrollEmailConfig.id}`)
})
