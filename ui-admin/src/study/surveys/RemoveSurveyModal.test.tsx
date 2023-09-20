import React from 'react'
import { mockConfiguredSurvey, mockStudyEnvContext } from 'test-utils/mocking-utils'
import  { setupRouterTest } from 'test-utils/router-testing-utils'
import { render, screen } from '@testing-library/react'
import RemoveSurveyModal from './RemoveSurveyModal'
import userEvent from '@testing-library/user-event'

describe('RemoveSurveyModal', () => {
  test('enables Remove button when confirmation prompt is filled out', async () => {
    //Arrange
    const user = userEvent.setup()
    const studyEnvContext = mockStudyEnvContext()
    const { RoutedComponent } = setupRouterTest(<RemoveSurveyModal
      studyEnvContext={studyEnvContext}
      show={true}
      setShow={jest.fn()}
      selectedSurveyConfig={mockConfiguredSurvey()}/>)
    render(RoutedComponent)

    //Act
    const confirmRemoveSurveyInput = screen.getByLabelText('Confirm by typing "remove Survey number one" below.')
    const removeButton = screen.getByText('Remove survey from Fake study: sandbox')
    expect(removeButton).toBeDisabled()

    await user.type(confirmRemoveSurveyInput, 'remove Survey number one')

    //Assert
    expect(removeButton).toBeEnabled()
  })
})
