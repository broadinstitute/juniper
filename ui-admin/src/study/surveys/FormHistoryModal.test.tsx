import { render, screen, waitFor } from '@testing-library/react'
import React from 'react'
import { mockStudyEnvContext, mockSurvey, mockSurveyVersionsList } from 'test-utils/mocking-utils'
import { select } from 'react-select-event'
import FormHistoryModal from './FormHistoryModal'

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
    render(<FormHistoryModal
      studyEnvContext={studyEnvContext}
      workingForm={mockSurvey()}
      onDismiss={jest.fn()}
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
