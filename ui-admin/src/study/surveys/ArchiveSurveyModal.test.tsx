import React from 'react'
import { mockConfiguredSurvey, mockStudyEnvContext } from 'test-utils/mocking-utils'
import { render, screen } from '@testing-library/react'
import ArchiveSurveyModal from './ArchiveSurveyModal'
import { userEvent } from '@testing-library/user-event'
import { setupRouterTest } from '@juniper/ui-core'

describe('ArchiveSurveyModal', () => {
  test('enables Archive button when confirmation prompt is filled out', async () => {
    //Arrange
    const user = userEvent.setup()
    const studyEnvContext = mockStudyEnvContext()
    const { RoutedComponent } = setupRouterTest(<ArchiveSurveyModal
      studyEnvContext={studyEnvContext}
      onDismiss={jest.fn()}
      selectedSurveyConfig={mockConfiguredSurvey()}/>)
    render(RoutedComponent)

    //Act
    const confirmArchiveSurveyInput = screen.getByLabelText('Confirm by typing "archive Survey number one" below.')
    const archiveButton = screen.getByText('Archive survey from Fake study: sandbox')
    expect(archiveButton).toBeDisabled()

    await user.type(confirmArchiveSurveyInput, 'archive Survey number one')

    //Assert
    expect(archiveButton).toBeEnabled()
  })
})
