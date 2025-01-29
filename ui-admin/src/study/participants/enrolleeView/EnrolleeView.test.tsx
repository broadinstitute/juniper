import React from 'react'

import { LoadedEnrolleeView } from './EnrolleeView'
import { mockEnrollee, mockStudyEnvContext, taskForForm } from 'test-utils/mocking-utils'
import { screen, within } from '@testing-library/react'
import { ParticipantTaskStatus, renderWithRouter } from '@juniper/ui-core'


test('renders survey links for configured surveys', async () => {
  jest.spyOn(window, 'alert').mockImplementation(jest.fn())
  const studyEnvContext = mockStudyEnvContext()
  const enrollee = mockEnrollee()

  renderWithRouter(<LoadedEnrolleeView enrollee={enrollee} studyEnvContext={studyEnvContext} onUpdate={jest.fn()}/>)
  const surveyLink = screen.getByText('Survey number one')
  // should have no badge since the enrollee hasn't take the survey
  expect(surveyLink.querySelector('span')).toBeNull()
})

test('renders survey task no response badge', async () => {
  jest.spyOn(window, 'alert').mockImplementation(jest.fn())
  const studyEnvContext = mockStudyEnvContext()
  const enrollee = mockEnrollee()
  enrollee.participantTasks
    .push(taskForForm(studyEnvContext.currentEnv.configuredSurveys[0].survey, enrollee.id, 'SURVEY'))

  renderWithRouter(<LoadedEnrolleeView enrollee={enrollee} studyEnvContext={studyEnvContext} onUpdate={jest.fn()}/>)
  const surveyLinkContainer = screen.getByText('Survey number one').parentElement as HTMLElement
  // should show a badge
  expect(within(surveyLinkContainer).getByTitle('No response')).toBeInTheDocument()
})

test('renders survey task viewed badge', async () => {
  jest.spyOn(window, 'alert').mockImplementation(jest.fn())
  const studyEnvContext = mockStudyEnvContext()
  const enrollee = mockEnrollee()
  enrollee.participantTasks
    .push({
      ...taskForForm(studyEnvContext.currentEnv.configuredSurveys[0].survey, enrollee.id, 'SURVEY'),
      status: 'VIEWED'
    })

  enrollee.surveyResponses.push({
    surveyId: studyEnvContext.currentEnv.configuredSurveys[0].surveyId,
    resumeData: '',
    answers: [],
    complete: false,
    enrolleeId: enrollee.id
  })

  renderWithRouter(<LoadedEnrolleeView enrollee={enrollee} studyEnvContext={studyEnvContext} onUpdate={jest.fn()}/>)
  const surveyLinkContainer = screen.getByText('Survey number one').parentElement as HTMLElement
  // should show a badge
  expect(within(surveyLinkContainer).getByTitle('Viewed')).toBeInTheDocument()
})

test('renders survey task complete badge for most recent', async () => {
  jest.spyOn(window, 'alert').mockImplementation(jest.fn())
  const studyEnvContext = mockStudyEnvContext()
  const enrollee = {
    ...mockEnrollee(),
    surveyResponses: [{
      id: 'response1',
      surveyId: studyEnvContext.currentEnv.configuredSurveys[0].surveyId,
      resumeData: '',
      answers: [],
      complete: false,
      enrolleeId: '1'
    }, {
      id: 'response2',
      surveyId: studyEnvContext.currentEnv.configuredSurveys[0].surveyId,
      resumeData: '',
      answers: [],
      complete: true,
      enrolleeId: '1'
    }],
    participantTasks: [
      taskForForm(studyEnvContext.currentEnv.configuredSurveys[0].survey, '1', 'SURVEY', 0, 'response1'),
      {
        ...taskForForm(studyEnvContext.currentEnv.configuredSurveys[0].survey, '1', 'SURVEY', 100000, 'response2'),
        status: 'COMPLETE' as ParticipantTaskStatus
      }
    ]
  }

  renderWithRouter(<LoadedEnrolleeView enrollee={enrollee} studyEnvContext={studyEnvContext} onUpdate={jest.fn()}/>)
  const surveyLinkContainer = screen.getByText('Survey number one').parentElement as HTMLElement
  // should show a badge
  expect(within(surveyLinkContainer).getByTitle('Complete')).toBeInTheDocument()
})
