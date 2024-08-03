import React from 'react'
import { mockEnrollee, mockStudyEnvContext } from 'test-utils/mocking-utils'
import { render, screen } from '@testing-library/react'
import AdvancedOptions from './AdvancedOptions'
import { userEvent } from '@testing-library/user-event'
import { setupRouterTest } from '@juniper/ui-core'

test('disables withdraw unless specific confirmation', async () => {
  const enrollee = mockEnrollee()
  const { RoutedComponent } = setupRouterTest(
    <AdvancedOptions enrollee={enrollee} studyEnvContext={mockStudyEnvContext()}/>)
  render(RoutedComponent)
  expect(screen.getByText('Withdraw')).toBeInTheDocument()
  expect(screen.getByText('Withdraw')).toHaveAttribute('aria-disabled', 'true')

  await userEvent.type(screen.getByRole('textbox'),
        `withdraw ${enrollee.profile.givenName} ${enrollee.profile.familyName}`)
  expect(screen.getByText('Withdraw')).toHaveAttribute('aria-disabled', 'false')
})
