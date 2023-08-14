import { render, screen } from '@testing-library/react'
import React from 'react'
import CreateSurveyModal from './CreateSurveyModal'
import { mockStudyEnvContext } from 'test-utils/mocking-utils'
import { setupRouterTest } from 'test-utils/router-testing-utils'
import userEvent from '@testing-library/user-event'

describe('CreateSurveyModal', () => {
  test('disables Create button when survey name and stable ID are blank', () => {
    //Arrange
    const studyEnvContext = mockStudyEnvContext()
    const { RoutedComponent } = setupRouterTest(<CreateSurveyModal
      studyEnvContext={studyEnvContext}
      isReadOnlyEnv={false}
      show={true}
      setShow={jest.fn()}/>)
    render(RoutedComponent)

    //Assert
    const createButton = screen.getByText('Create')
    expect(createButton).toBeDisabled()
  })

  test('enables Create button when survey name and stable ID are filled out', async () => {
    //Arrange
    const user = userEvent.setup()
    const studyEnvContext = mockStudyEnvContext()
    const { RoutedComponent } = setupRouterTest(<CreateSurveyModal
      studyEnvContext={studyEnvContext}
      isReadOnlyEnv={false}
      show={true}
      setShow={jest.fn()}/>)
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
})
