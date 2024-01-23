import { render, screen, waitFor } from '@testing-library/react'
import React from 'react'
import { mockStudyEnvContext, mockSurvey, mockSurveyVersionsList } from 'test-utils/mocking-utils'
import FormOptions from './FormOptions'
import { select } from 'react-select-event'

jest.mock('api/api', () => ({
  getSurveyVersions: () => {
    return Promise.resolve(mockSurveyVersionsList())
  },
  getSurvey: () => {
    return Promise.resolve(mockSurvey())
  }
}))

describe('VersionSelector', () => {
  test('renders a list of form versions that can be selected', async () => {
    //Arrange
    const studyEnvContext = mockStudyEnvContext()
    render(<FormOptions
      studyEnvContext={studyEnvContext}
      form={mockSurvey()}
      onDismiss={jest.fn()}
      isDirty={false}
      visibleVersionPreviews={[]}
      setVisibleVersionPreviews={jest.fn()}
    />)

    //Act
    await waitFor(() => expect(screen.queryByTestId('loading-spinner')).not.toBeInTheDocument())
    await select(screen.getByLabelText('Select version to preview'), ['1'])
    const openPreviewButton = screen.getByText('View preview')
    const openEditorLink = screen.getByText('Open read-only editor')

    //Assert
    expect(openPreviewButton).toBeEnabled()
    expect(openEditorLink).toBeEnabled()
    expect(openEditorLink)
      .toHaveAttribute('href', '/portalCode/studies/fakeStudy/env/sandbox/forms/surveys/survey1/1?readOnly=true')
  })
})
