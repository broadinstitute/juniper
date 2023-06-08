import React from 'react'

import EnrolleeView from './EnrolleeView'
import { setupRouterTest } from 'test-utils/router-testing-utils'
import { mockEnrollee, mockStudyEnvContext, taskForSurvey } from 'test-utils/mocking-utils'
import { render, screen } from '@testing-library/react'


test('renders survey links for configured surveys', async () => {
  const studyEnvContext = mockStudyEnvContext()
  const enrollee = mockEnrollee()

  const { RoutedComponent } = setupRouterTest(
    // eslint-disable-next-line @typescript-eslint/no-empty-function
    <EnrolleeView enrollee={enrollee} studyEnvContext={studyEnvContext} onUpdate={() => {}}/>)
  render(RoutedComponent)
  const surveyLink = screen.getByText('Survey number one')
  // should have no badge since the enrollee hasn't take the survey
  expect(surveyLink.querySelector('span')).toBeNull()
})


test('renders survey taken badges', async () => {
  const studyEnvContext = mockStudyEnvContext()
  const enrollee = mockEnrollee()
  enrollee.surveyResponses.push({
    surveyId: studyEnvContext.currentEnv.configuredSurveys[0].surveyId,
    resumeData: '',
    answers: [],
    complete: false,
    enrolleeId: enrollee.id
  })
  enrollee.participantTasks.push(taskForSurvey(studyEnvContext.currentEnv.configuredSurveys[0].survey, enrollee.id))

  const { RoutedComponent } = setupRouterTest(
    // eslint-disable-next-line @typescript-eslint/no-empty-function
    <EnrolleeView enrollee={enrollee} studyEnvContext={studyEnvContext} onUpdate={() => {}}/>)
  render(RoutedComponent)
  const surveyLink = screen.getByText('Survey number one')
  // should show a badge
  expect(surveyLink.querySelector('span')).toHaveTextContent('1')
})


