import { render, screen } from '@testing-library/react'
import React from 'react'
import { mockPortalEnvContext } from 'test-utils/mocking-utils'
import { userEvent } from '@testing-library/user-event'
import CreatePreRegSurveyModal from './CreatePreRegSurveyModal'
import { setupRouterTest } from '@juniper/ui-core'

describe('CreatePreRegSurveyModal', () => {
  test('disables Create button when survey name and stable ID are blank', () => {
    const portalEnvContext = mockPortalEnvContext('sandbox')
    const { RoutedComponent } = setupRouterTest(<CreatePreRegSurveyModal
      portalEnvContext={portalEnvContext}
      onDismiss={jest.fn()}/>)
    render(RoutedComponent)

    const createButton = screen.getByText('Create')
    expect(createButton).toBeDisabled()
  })

  test('enables Create button when survey name and stable ID are filled out', async () => {
    const user = userEvent.setup()
    const portalEnvContext = mockPortalEnvContext('sandbox')
    const { RoutedComponent } = setupRouterTest(<CreatePreRegSurveyModal
      portalEnvContext={portalEnvContext}
      onDismiss={jest.fn()}/>)
    render(RoutedComponent)

    const surveyNameInput = screen.getByLabelText('Survey Name')
    const surveyStableIdInput = screen.getByLabelText('Survey Stable ID')
    await user.type(surveyNameInput, 'Test Survey')
    await user.type(surveyStableIdInput, 'test_survey_id')

    const createButton = screen.getByText('Create')
    expect(createButton).toBeEnabled()
  })

  test('should autofill the stable ID as the user fills in the survey name', async () => {
    const user = userEvent.setup()
    const portalEnvContext = mockPortalEnvContext('sandbox')
    const { RoutedComponent } = setupRouterTest(<CreatePreRegSurveyModal
      portalEnvContext={portalEnvContext}
      onDismiss={jest.fn()}/>)
    render(RoutedComponent)

    const surveyNameInput = screen.getByLabelText('Survey Name')
    const surveyStableIdInput = screen.getByLabelText('Survey Stable ID')
    await user.type(surveyNameInput, 'Test Survey')

    //Confirm that auto-fill stable ID worked
    expect(surveyStableIdInput).toHaveValue('testSurvey')
  })
})
