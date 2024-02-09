import { render, screen, waitFor } from '@testing-library/react'
import React from 'react'
import { mockStudyEnvContext, mockSurvey, mockSurveyVersionsList } from 'test-utils/mocking-utils'
import FormOptions from './FormOptions'
import { select } from 'react-select-event'
import userEvent from '@testing-library/user-event'

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
    const studyEnvContext = mockStudyEnvContext()
    render(<FormOptions
      studyEnvContext={studyEnvContext}
      workingForm={mockSurvey()}
      updateWorkingForm={jest.fn()}
      onDismiss={jest.fn()}
      isDirty={false}
      visibleVersionPreviews={[]}
      setVisibleVersionPreviews={jest.fn()}
    />)

    await waitFor(() => expect(screen.queryByTestId('loading-spinner')).not.toBeInTheDocument())
    await select(screen.getByLabelText('Other versions'), ['1'])
    const openPreviewButton = screen.getByText('View preview')
    const openEditorLink = screen.getByText('Open read-only editor')

    expect(openPreviewButton).toBeEnabled()
    expect(openEditorLink).toBeEnabled()
    expect(openEditorLink)
      .toHaveAttribute('href', '/portalCode/studies/fakeStudy/env/sandbox/forms/surveys/survey1/1?readOnly=true')
  })
})


describe('FormOptions', () => {
  test('allows changing a survey to be required', async () => {
    const updateWorkingForm = jest.fn()
    render(<FormOptions
      studyEnvContext={mockStudyEnvContext()}
      workingForm={mockSurvey()}
      updateWorkingForm={updateWorkingForm}
      onDismiss={jest.fn()}
      isDirty={false}
      visibleVersionPreviews={[]}
      setVisibleVersionPreviews={jest.fn()}
    />)

    await userEvent.click(screen.getByLabelText('Required'))
    expect(updateWorkingForm).toHaveBeenCalledWith({
      ...mockSurvey(),
      required: true
    })
  })

  test('allows changing a survey to be auto-updating of versions', async () => {
    const updateWorkingForm = jest.fn()
    render(<FormOptions
      studyEnvContext={mockStudyEnvContext()}
      workingForm={mockSurvey()}
      updateWorkingForm={updateWorkingForm}
      onDismiss={jest.fn()}
      isDirty={false}
      visibleVersionPreviews={[]}
      setVisibleVersionPreviews={jest.fn()}
    />)

    await userEvent.click(screen.getByLabelText('Auto-update participant tasks', { exact: false }))
    expect(updateWorkingForm).toHaveBeenCalledWith({
      ...mockSurvey(),
      autoUpdateTaskAssignments: true
    })
  })

  test('allows the user to download the JSON file', async () => {
    //Arrange
    render(<FormOptions
      studyEnvContext={mockStudyEnvContext()}
      workingForm={mockSurvey()}
      updateWorkingForm={jest.fn()}
      onDismiss={jest.fn()}
      isDirty={false}
      visibleVersionPreviews={[]}
      setVisibleVersionPreviews={jest.fn()}
    />)

    //Act
    const downloadButton = screen.getByRole('button', { name: 'Download JSON' })
    downloadButton.click()

    //Assert
    expect(downloadButton).toBeInTheDocument()
    expect(downloadButton).toBeEnabled()
  })
})
