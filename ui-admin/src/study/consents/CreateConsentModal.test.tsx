import { render, screen } from '@testing-library/react'
import React from 'react'
import CreateConsentModal from './CreateConsentModal'
import { mockStudyEnvContext } from 'test-utils/mocking-utils'
import { setupRouterTest } from 'test-utils/router-testing-utils'
import userEvent from '@testing-library/user-event'

describe('CreateConsentModal', () => {
  test('disables Create button when survey name and stable ID are blank', () => {
    const studyEnvContext = mockStudyEnvContext()
    const { RoutedComponent } = setupRouterTest(<CreateConsentModal
      studyEnvContext={studyEnvContext}
      onDismiss={jest.fn()}/>)
    render(RoutedComponent)

    const createButton = screen.getByText('Create')
    expect(createButton).toBeDisabled()
  })

  test('enables Create button when survey name and stable ID are filled out', async () => {
    const user = userEvent.setup()
    const studyEnvContext = mockStudyEnvContext()
    const { RoutedComponent } = setupRouterTest(<CreateConsentModal
      studyEnvContext={studyEnvContext}
      onDismiss={jest.fn()}/>)
    render(RoutedComponent)

    const surveyNameInput = screen.getByLabelText('Consent Name')
    const surveyStableIdInput = screen.getByLabelText('Consent Stable ID')
    await user.type(surveyNameInput, 'Test consent 123')
    await user.type(surveyStableIdInput, 'test_consent_id')

    const createButton = screen.getByText('Create')
    expect(createButton).toBeEnabled()
  })

  test('should autofill the stable ID as the user fills in the survey name', async () => {
    const user = userEvent.setup()
    const studyEnvContext = mockStudyEnvContext()
    const { RoutedComponent } = setupRouterTest(<CreateConsentModal
      studyEnvContext={studyEnvContext}
      onDismiss={jest.fn()}/>)
    render(RoutedComponent)

    await user.type(screen.getByLabelText('Consent Name'), 'Test Consent 1')

    //Confirm that auto-fill stable ID worked
    expect(screen.getByLabelText('Consent Stable ID')).toHaveValue('testConsent1')
  })
})
