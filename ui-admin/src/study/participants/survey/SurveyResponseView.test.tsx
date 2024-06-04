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
  const mockResponseWithAnswer = {
    ...mockSurveyResponse(),
    answers: [
      mockAnswer()
    ]
  }

  test('Printing mode shows the download/print modal', async () => {
    const printSpy = jest.spyOn(window, 'print').mockImplementation(() => 1)
    const { RoutedComponent } = setupRouterTest(
      <RawEnrolleeSurveyView enrollee={mockEnrollee()}
        studyEnvContext={mockStudyEnvContext()} response={mockResponseWithAnswer}
        configSurvey={mockConfiguredSurvey()} onUpdate={jest.fn()}/>)
    render(RoutedComponent)
    await userEvent.click(screen.getByText('Printing'))
    await waitFor(() => expect(screen.getByText('Done')).toBeVisible())
    await waitFor(() => expect(printSpy).toHaveBeenCalledTimes(1))
  })

  test('Viewing mode shows survey response view', async () => {
    const { RoutedComponent } = setupRouterTest(
      <RawEnrolleeSurveyView enrollee={mockEnrollee()}
        studyEnvContext={mockStudyEnvContext()} response={mockResponseWithAnswer}
        configSurvey={mockConfiguredSurvey()} onUpdate={jest.fn()}/>)
    render(RoutedComponent)
    const viewingElements = screen.getAllByText('Viewing')
    expect(viewingElements).toHaveLength(2)
    await waitFor(() => expect(screen.getByText('Show all questions')).toBeVisible())
  })

  test('Editing mode shows the survey response editor', async () => {
    const { RoutedComponent } = setupRouterTest(
      <RawEnrolleeSurveyView enrollee={mockEnrollee()}
        studyEnvContext={mockStudyEnvContext()} response={mockResponseWithAnswer}
        configSurvey={mockConfiguredSurvey()} onUpdate={jest.fn()}/>)
    render(RoutedComponent)
    await userEvent.click(screen.getByText('Editing'))
    await waitFor(() => expect(screen.queryByText('Show all questions')).not.toBeInTheDocument())
  })
})
