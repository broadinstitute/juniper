import React from 'react'
import { render, screen, waitFor } from '@testing-library/react'

import { mockTrigger, mockPortalContext, mockStudyEnvContext } from 'test-utils/mocking-utils'
import TriggerList from './TriggerList'
import { userEvent } from '@testing-library/user-event'
import Api from 'api/api'
import { ReactNotifications } from 'react-notifications-component'
import { setupRouterTest } from '@juniper/ui-core'

test('renders routable trigger list', async () => {
  const studyEnvContext = mockStudyEnvContext()
  const enrollEmailConfig = {
    ...mockTrigger(),
    id: 'event1',
    triggerType: 'EVENT',
    eventType: 'STUDY_ENROLLMENT'
  }
  const triggers = [
    enrollEmailConfig,
    {
      ...mockTrigger(),
      id: 'reminder1',
      triggerType: 'TASK_REMINDER',
      taskType: 'CONSENT'
    },
    {
      ...mockTrigger(),
      id: 'reminder2',
      triggerType: 'TASK_REMINDER',
      taskType: 'SURVEY'
    }
  ]
  jest.spyOn(Api, 'findTriggersForStudyEnv')
    .mockImplementation(() => Promise.resolve(triggers))
  jest.spyOn(Api, 'findTrigger')
    .mockImplementation(jest.fn())

  const { RoutedComponent, router } =
      setupRouterTest(<>
        <ReactNotifications/>
        <TriggerList studyEnvContext={studyEnvContext} portalContext={mockPortalContext()}/>
      </>)
  render(RoutedComponent)
  expect(screen.getByText('Participant email configuration')).toBeInTheDocument()
  await waitFor(() => expect(screen.getByText('Study enrollment')).toBeInTheDocument())
  expect(screen.getByText('Reminder: CONSENT')).toBeInTheDocument()
  expect(screen.getByText('Reminder: SURVEY')).toBeInTheDocument()

  await userEvent.click(screen.getByText('Study enrollment'))
  expect(router.state.location.pathname).toEqual(`/triggers/${enrollEmailConfig.id}`)
})

test('allows deletion of notification config', async () => {
  const studyEnvContext = mockStudyEnvContext()
  const consentConfig = {
    ...mockTrigger(),
    id: 'reminder1',
    triggerType: 'TASK_REMINDER',
    taskType: 'CONSENT'
  }
  const notificationConfigs = [
    {
      ...mockTrigger(),
      id: 'event1',
      triggerType: 'EVENT',
      eventType: 'STUDY_ENROLLMENT'
    },
    consentConfig,
    {
      ...mockTrigger(),
      id: 'reminder2',
      triggerType: 'TASK_REMINDER',
      taskType: 'SURVEY'
    }
  ]
  jest.spyOn(Api, 'findTriggersForStudyEnv')
    .mockImplementation(() => Promise.resolve(notificationConfigs))
  jest.spyOn(Api, 'findTrigger')
    .mockImplementation(() => Promise.resolve(consentConfig))
  jest.spyOn(Api, 'deleteTrigger').mockImplementation(() => Promise.resolve(new Response()))

  const { RoutedComponent } =
    setupRouterTest(<>
      <ReactNotifications/>
      <TriggerList studyEnvContext={studyEnvContext} portalContext={mockPortalContext()}/>
    </>)
  render(RoutedComponent)

  await waitFor(() => expect(screen.getByText('Study enrollment')).toBeInTheDocument())

  expect(screen.getByText('Reminder: CONSENT')).toBeInTheDocument()
  await userEvent.click(screen.getByText('Reminder: CONSENT'))

  await waitFor(() => expect(screen.getByText('Delete')).toBeInTheDocument())

  await userEvent.click(screen.getByText('Delete'))

  await waitFor(() => expect(screen.getByText('Delete Notification Config')).toBeInTheDocument())

  // modal has popped up now, so delete
  const deleteButtons = screen.getAllByText('Delete')

  // find the button which is inside a modal
  const modalButton = deleteButtons.find(btn => btn.closest('.modal-footer'))

  expect(modalButton).not.toBeUndefined()

  if (modalButton) {
    await userEvent.click(modalButton)
  }

  await waitFor(
    () =>
      expect(Api.deleteTrigger)
        .toHaveBeenCalledWith(
          studyEnvContext.portal.shortcode,
          studyEnvContext.study.shortcode,
          studyEnvContext.currentEnv.environmentName,
          consentConfig.id
        ))
})
