import React from 'react'
import { screen, waitFor } from '@testing-library/react'

import {
  mockStudyEnvContext,
  mockNotification,
  renderInPortalRouter, mockTrigger
} from 'test-utils/mocking-utils'

import Api from 'api/api'
import TriggerNotifications from './TriggerNotifications'

test('renders trigger notification list', async () => {
  const studyEnvContext = mockStudyEnvContext()
  const trigger = mockTrigger()
  studyEnvContext.currentEnv.triggers.push(trigger)
  const notifications = [
    {
      ...mockNotification(),
      sentTo: 'someone@test.com'
    }
  ]
  jest.spyOn(Api, 'fetchTriggerNotifications')
    .mockImplementation(() => Promise.resolve(notifications))

  renderInPortalRouter(studyEnvContext.portal, <TriggerNotifications studyEnvContext={studyEnvContext} />)
  await waitFor(() => expect(screen.getByText('someone@test.com')).toBeInTheDocument())
})
