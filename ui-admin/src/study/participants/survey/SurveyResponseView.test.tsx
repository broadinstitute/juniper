import {
  renderWithRouter,
  setupRouterTest
} from '@juniper/ui-core'
import {
  mockAnswer,
  mockConfiguredSurvey,
  mockEnrollee,
  mockParticipantTask,
  mockStudyEnvContext,
  mockSurveyResponse
} from 'test-utils/mocking-utils'
import {
  render,
  screen,
  waitFor
} from '@testing-library/react'
import React from 'react'
import { userEvent } from '@testing-library/user-event'
import SurveyResponseView, { RawEnrolleeSurveyView } from './SurveyResponseView'
import { userHasPermission } from 'user/UserProvider'
import Api from 'api/api'

jest.mock('user/UserProvider', () => ({
  ...jest.requireActual('user/UserProvider'),
  userHasPermission: jest.fn()
}))

jest.spyOn(Api, 'fetchEnrolleeChangeRecords').mockResolvedValue([])

describe('SurveyResponseView', () => {
  test('Displays assignment button if unassigned', async () => {
    renderWithRouter(<SurveyResponseView enrollee={mockEnrollee()}
      updateResponseMap={jest.fn()}
      studyEnvContext={mockStudyEnvContext()}
      responseMap={{
        someSurvey: {
          tasks: [], responses: [],
          survey: mockConfiguredSurvey()
        }
      }} onUpdate={jest.fn()}/>, ['/someSurvey'], ':surveyStableId')
    expect(screen.getByText('Not assigned')).toBeVisible()
    expect(screen.getByText('Assign')).toBeVisible()
  })

  test('Displays response if assigned', async () => {
    renderWithRouter(<SurveyResponseView enrollee={mockEnrollee()}
      updateResponseMap={jest.fn()}
      studyEnvContext={mockStudyEnvContext()}
      responseMap={{
        someSurvey: {
          tasks: [mockParticipantTask('SURVEY', 'NEW')], responses: [],
          survey: mockConfiguredSurvey()
        }
      }} onUpdate={jest.fn()}/>, ['/someSurvey'], ':surveyStableId')
    expect(screen.getByText('Not Started')).toBeVisible()
  })
})


describe('RawEnrolleeSurveyView', () => {
  jest.clearAllMocks()
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
        task={mockParticipantTask('SURVEY', 'COMPLETE')}
        updateResponseMap={jest.fn()}
        studyEnvContext={mockStudyEnvContext()} response={mockResponseWithAnswer}
        configSurvey={mockConfiguredSurvey()} onUpdate={jest.fn()}/>)
    render(RoutedComponent)
    await userEvent.click(screen.getByText('Printing'))
    await waitFor(() => expect(screen.getByText('Download / Print')).toBeVisible())
    await waitFor(() => expect(printSpy).toHaveBeenCalledTimes(0))
    await userEvent.click(screen.getByText('Download / Print'))
    await waitFor(() => expect(printSpy).toHaveBeenCalledTimes(1))
  })

  test('Viewing mode shows survey response view', async () => {
    const { RoutedComponent } = setupRouterTest(
      <RawEnrolleeSurveyView enrollee={mockEnrollee()}
        task={mockParticipantTask('SURVEY', 'COMPLETE')}
        updateResponseMap={jest.fn()}
        studyEnvContext={mockStudyEnvContext()} response={mockResponseWithAnswer}
        configSurvey={mockConfiguredSurvey()} onUpdate={jest.fn()}/>)
    render(RoutedComponent)
    const viewingElements = screen.getAllByText('Viewing')
    expect(viewingElements).toHaveLength(2)
    await waitFor(() => expect(screen.getByText('Show all questions')).toBeVisible())
  })

  test('Editing mode shows the survey response editor after providing a justification', async () => {
    (userHasPermission as jest.Mock).mockImplementation(() => {
      return true
    })
    const { RoutedComponent } = setupRouterTest(
      <RawEnrolleeSurveyView enrollee={mockEnrollee()}
        task={mockParticipantTask('SURVEY', 'COMPLETE')}
        updateResponseMap={jest.fn()}
        studyEnvContext={mockStudyEnvContext()} response={mockResponseWithAnswer}
        configSurvey={mockConfiguredSurvey()} onUpdate={jest.fn()}/>)
    render(RoutedComponent)

    await userEvent.click(screen.getByText('Editing'))
    await waitFor(() => expect(screen.getByText('Add Justification')).toBeVisible())
    await userEvent.type(screen.getByRole('textbox'), 'This is a test justification')
    await userEvent.click(screen.getByText('Continue to edit'))
    await waitFor(() => expect(screen.queryByText('Show all questions')).not.toBeInTheDocument())
  })
})
