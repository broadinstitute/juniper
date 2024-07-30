import { render, screen, waitFor } from '@testing-library/react'
import React from 'react'
import { mockStudyEnvContext, mockSurveyVersionsList } from 'test-utils/mocking-utils'
import { select } from 'react-select-event'
import FormHistoryModal from './FormHistoryModal'
import Api from 'api/api'

describe('VersionSelector', () => {
  test('renders a list of form versions that can be selected', async () => {
    const surveyList = mockSurveyVersionsList()
    jest.spyOn(Api, 'getSurveyVersions').mockResolvedValue(surveyList)

    const studyEnvContext = mockStudyEnvContext()
    render(<FormHistoryModal
      studyEnvContext={studyEnvContext}
      workingForm={surveyList[1]}
      onDismiss={jest.fn()}
      replaceSurvey={jest.fn()}
    />)

    await waitFor(() => expect(screen.queryByTestId('loading-spinner')).not.toBeInTheDocument())
    await select(screen.getByLabelText('Other versions'), ['1'])
    const openPreviewButton = screen.getByText('View/Edit version 1')
    const switchVersionButton = screen.getByText('Switch sandbox to version 1')
    expect(openPreviewButton).toBeEnabled()
    expect(switchVersionButton).toBeEnabled()
    expect(openPreviewButton)
      .toHaveAttribute('href', '/portalCode/studies/fakeStudy/env/sandbox/forms/surveys/survey1/1')
  })
})
