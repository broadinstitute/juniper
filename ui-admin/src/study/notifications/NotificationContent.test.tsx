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
  jest.spyOn(Api, 'findNotificationConfig')
    .mockImplementation(jest.fn())

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

test('allows deletion of notification config', async () => {
  const studyEnvContext = mockStudyEnvContext()
  const consentConfig = {
    ...mockNotificationConfig(),
    id: 'reminder1',
    notificationType: 'TASK_REMINDER',
    taskType: 'CONSENT'
  }
  const notificationConfigs = [
    {
      ...mockNotificationConfig(),
      id: 'event1',
      notificationType: 'EVENT',
      eventType: 'STUDY_ENROLLMENT'
    },
    consentConfig,
    {
      ...mockNotificationConfig(),
      id: 'reminder2',
      notificationType: 'TASK_REMINDER',
      taskType: 'SURVEY'
    }
  ]
  jest.spyOn(Api, 'findNotificationConfigsForStudyEnv')
    .mockImplementation(() => Promise.resolve(notificationConfigs))
  jest.spyOn(Api, 'findNotificationConfig')
    .mockImplementation(() => Promise.resolve(consentConfig))
  jest.spyOn(Api, 'deleteNotificationConfig').mockImplementation(() => Promise.resolve(new Response()))

  const { RoutedComponent } =
    setupRouterTest(<>
      <ReactNotifications/>
      <NotificationContent studyEnvContext={studyEnvContext} portalContext={mockPortalContext()}/>
    </>)
  render(RoutedComponent)

  await waitFor(() => expect(screen.getByText('Study enrollment')).toBeInTheDocument())

  expect(screen.getByText('Reminder: CONSENT')).toBeInTheDocument()
  await userEvent.click(screen.getByText('Reminder: CONSENT'))

  await waitFor(() => expect(screen.getByText('Delete')).toBeInTheDocument())

  await userEvent.click(screen.getByText('Delete'))

  await waitFor(() => expect(screen.getByText('Delete Notification Config')).toBeInTheDocument())

  //screen.debug(screen.getByText('Delete Notification Config\n'))
  // modal has popped up now

  const deleteButtons = screen.getAllByText('Delete')

  // find the button which is inside a modal
  const modalButton = deleteButtons.find(btn => btn.closest('.modal-footer'))

  expect(modalButton).not.toBeUndefined()

  if (modalButton) {
    await userEvent.click(modalButton)
  }

  await waitFor(
    () =>
      expect(Api.deleteNotificationConfig)
        .toBeCalledWith(
          studyEnvContext.portal.shortcode,
          studyEnvContext.study.shortcode,
          studyEnvContext.currentEnv.environmentName,
          consentConfig.id
        ))
})
