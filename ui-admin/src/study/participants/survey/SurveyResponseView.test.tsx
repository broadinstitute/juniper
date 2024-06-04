import { setupRouterTest } from '@juniper/ui-core'
import {
  mockAnswer,
  mockConfiguredSurvey,
  mockEnrollee,
  mockStudyEnvContext,
  mockSurveyResponse
} from 'test-utils/mocking-utils'
import { render, screen, waitFor } from '@testing-library/react'
import React from 'react'
import userEvent from '@testing-library/user-event'
import { RawEnrolleeSurveyView } from './SurveyResponseView'

describe('RawEnrolleeSurveyView', () => {
  test('shows the download/print modal', async () => {
    const enrollee = {
      ...mockEnrollee(),
      surveyResponses: [
        {
          ...mockSurveyResponse(),
          answers: [
            mockAnswer()
          ]
        }
      ]
    }
    const printSpy = jest.spyOn(window, 'print').mockImplementation(() => 1)
    const { RoutedComponent } = setupRouterTest(
      <RawEnrolleeSurveyView enrollee={enrollee} studyEnvContext={mockStudyEnvContext()}
        configSurvey={mockConfiguredSurvey()} onUpdate={jest.fn()}/>)
    render(RoutedComponent)
    await userEvent.click(screen.getByText('Printing'))
    await waitFor(() => expect(screen.getByText('Done')).toBeVisible())
    await waitFor(() => expect(printSpy).toHaveBeenCalledTimes(1))
  })
})

//configSurvey={{ ...mockConfiguredSurvey(), survey: { ...mockSurvey(), surveyType: 'ADMIN'}}} onUpdate={jest.fn()}/>)
