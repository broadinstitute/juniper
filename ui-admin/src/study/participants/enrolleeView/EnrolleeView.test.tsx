import React from 'react'

import { LoadedEnrolleeView } from './EnrolleeView'
import { mockEnrollee, mockStudyEnvContext, taskForForm } from 'test-utils/mocking-utils'
import { render, screen, within } from '@testing-library/react'
import { setupRouterTest } from '@juniper/ui-core'


test('renders survey links for configured surveys', async () => {
  jest.spyOn(window, 'alert').mockImplementation(jest.fn())
  const studyEnvContext = mockStudyEnvContext()
  const enrollee = mockEnrollee()

  const { RoutedComponent } = setupRouterTest(
    // eslint-disable-next-line @typescript-eslint/no-empty-function
    <LoadedEnrolleeView enrollee={enrollee} studyEnvContext={studyEnvContext} onUpdate={() => {}}/>)
  render(RoutedComponent)
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

  const { RoutedComponent } = setupRouterTest(
    <LoadedEnrolleeView enrollee={enrollee} studyEnvContext={studyEnvContext} onUpdate={jest.fn()}/>)
  render(RoutedComponent)
  const surveyLinkContainer = screen.getByText('Survey number one').parentElement as HTMLElement
  // should show a badge
  expect(within(surveyLinkContainer).getByTitle('No response')).toBeInTheDocument()
})

test('renders survey task viewed badge', async () => {
  jest.spyOn(window, 'alert').mockImplementation(jest.fn())
  const studyEnvContext = mockStudyEnvContext()
  const enrollee = mockEnrollee()
  enrollee.participantTasks
    .push(taskForForm(studyEnvContext.currentEnv.configuredSurveys[0].survey, enrollee.id, 'SURVEY'))

  enrollee.surveyResponses.push({
    surveyId: studyEnvContext.currentEnv.configuredSurveys[0].surveyId,
    resumeData: '',
    answers: [],
    complete: false,
    enrolleeId: enrollee.id
  })

  const { RoutedComponent } = setupRouterTest(
    <LoadedEnrolleeView enrollee={enrollee} studyEnvContext={studyEnvContext} onUpdate={jest.fn()}/>)
  render(RoutedComponent)
  const surveyLinkContainer = screen.getByText('Survey number one').parentElement as HTMLElement
  // should show a badge
  expect(within(surveyLinkContainer).getByTitle('Viewed')).toBeInTheDocument()
})
