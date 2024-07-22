import { render, screen } from '@testing-library/react'
import React from 'react'
import { mockStudyEnvContext } from 'test-utils/mocking-utils'
import { userEvent } from '@testing-library/user-event'
import CreatePreEnrollSurveyModal from './CreatePreEnrollSurveyModal'
import { setupRouterTest } from '@juniper/ui-core'

describe('CreatePreEnrollSurveyModal', () => {
  test('disables Create button when survey name and stable ID are blank', () => {
    //Arrange
    const studyEnvContext = mockStudyEnvContext()
    const { RoutedComponent } = setupRouterTest(<CreatePreEnrollSurveyModal
      studyEnvContext={studyEnvContext}
      onDismiss={jest.fn()}/>)
    render(RoutedComponent)

    //Assert
    const createButton = screen.getByText('Create')
    expect(createButton).toBeDisabled()
  })

  test('enables Create button when survey name and stable ID are filled out', async () => {
    //Arrange
    const user = userEvent.setup()
    const studyEnvContext = mockStudyEnvContext()
    const { RoutedComponent } = setupRouterTest(<CreatePreEnrollSurveyModal
      studyEnvContext={studyEnvContext}
      onDismiss={jest.fn()}/>)
    render(RoutedComponent)

    //Act
    const surveyNameInput = screen.getByLabelText('Survey Name')
    const surveyStableIdInput = screen.getByLabelText('Survey Stable ID')
    await user.type(surveyNameInput, 'Test Survey')
    await user.type(surveyStableIdInput, 'test_survey_id')

    //Assert
    const createButton = screen.getByText('Create')
    expect(createButton).toBeEnabled()
  })

  test('should autofill the stable ID as the user fills in the survey name', async () => {
    //Arrange
    const user = userEvent.setup()
    const studyEnvContext = mockStudyEnvContext()
    const { RoutedComponent } = setupRouterTest(<CreatePreEnrollSurveyModal
      studyEnvContext={studyEnvContext}
      onDismiss={jest.fn()}/>)
    render(RoutedComponent)

    //Act
    const surveyNameInput = screen.getByLabelText('Survey Name')
    const surveyStableIdInput = screen.getByLabelText('Survey Stable ID')
    await user.type(surveyNameInput, 'Test Survey')

    //Assert
    //Confirm that auto-fill stable ID worked
    expect(surveyStableIdInput).toHaveValue('testSurvey')
  })
})
