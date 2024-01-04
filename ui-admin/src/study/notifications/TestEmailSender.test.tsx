import React from 'react'
import { render, screen } from '@testing-library/react'

import { mockNotificationConfig } from 'test-utils/mocking-utils'
import { setupRouterTest } from 'test-utils/router-testing-utils'
import { mockAdminUser, MockUserProvider } from 'test-utils/user-mocking-utils'
import TestEmailSender from './TestEmailSender'

test('replaces the email with the username', async () => {
  const { RoutedComponent } =
        setupRouterTest(
          <MockUserProvider user={{ ...mockAdminUser(false), username: 'admin@user.com' }}>
            <TestEmailSender studyEnvParams={{ portalShortcode: 'portal', envName: 'irb', studyShortcode: 'study' }}
              onDismiss={jest.fn()}
              notificationConfig={mockNotificationConfig()}/>
          </MockUserProvider>
        )
  render(RoutedComponent)
  expect((screen.getByRole('textbox') as HTMLTextAreaElement).value)
    .toContain('"contactEmail": "admin@user.com"')
})
