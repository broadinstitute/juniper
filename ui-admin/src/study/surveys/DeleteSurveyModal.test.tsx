import React from 'react'
import { mockConfiguredSurvey, mockStudyEnvContext } from 'test-utils/mocking-utils'
import  { setupRouterTest } from 'test-utils/router-testing-utils'
import { render, screen } from '@testing-library/react'
import DeleteSurveyModal from './DeleteSurveyModal'
import userEvent from '@testing-library/user-event'

describe('DeleteSurveyModal', () => {
  test('enables Delete button when confirmation prompt is filled out', async () => {
    //Arrange
    const user = userEvent.setup()
    const studyEnvContext = mockStudyEnvContext()
    const { RoutedComponent } = setupRouterTest(<DeleteSurveyModal
      studyEnvContext={studyEnvContext}
      show={true}
      setShow={jest.fn()}
      selectedSurveyConfig={mockConfiguredSurvey()}/>)
    render(RoutedComponent)

    //Act
    //gets the confirmation prompt input
    const confirmDeleteSurveyInput = screen.getByLabelText('Confirm by typing "delete Survey number one" below.')
    const deleteButton = screen.getByText('Delete survey')
    expect(deleteButton).toBeDisabled()

    await user.type(confirmDeleteSurveyInput, 'delete Survey number one')

    //Assert
    expect(deleteButton).toBeEnabled()
  })
})
