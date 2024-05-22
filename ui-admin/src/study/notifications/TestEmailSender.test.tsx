import React from 'react'
import { render, screen } from '@testing-library/react'

import { mockTrigger } from 'test-utils/mocking-utils'
import { mockAdminUser, MockUserProvider } from 'test-utils/user-mocking-utils'
import TestEmailSender from './TestEmailSender'
import { setupRouterTest } from '@juniper/ui-core'

test('replaces the email with the username', async () => {
  const { RoutedComponent } =
        setupRouterTest(
          <MockUserProvider user={{ ...mockAdminUser(false), username: 'admin@user.com' }}>
            <TestEmailSender studyEnvParams={{ portalShortcode: 'portal', envName: 'irb', studyShortcode: 'study' }}
              onDismiss={jest.fn()}
              trigger={mockTrigger()}/>
          </MockUserProvider>
        )
  render(RoutedComponent)
  expect((screen.getByRole('textbox') as HTMLTextAreaElement).value)
    .toContain('"contactEmail": "admin@user.com"')
})
