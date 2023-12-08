import React from 'react'

import { LoadedEnrolleeView } from './EnrolleeView'
import { setupRouterTest } from 'test-utils/router-testing-utils'
import { mockEnrollee, mockStudyEnvContext, taskForForm } from 'test-utils/mocking-utils'
import { render, screen, within } from '@testing-library/react'


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

test('renders survey taken badges', async () => {
  jest.spyOn(window, 'alert').mockImplementation(jest.fn())
  const studyEnvContext = mockStudyEnvContext()
  const enrollee = mockEnrollee()
  enrollee.surveyResponses.push({
    surveyId: studyEnvContext.currentEnv.configuredSurveys[0].surveyId,
    resumeData: '',
    answers: [],
    complete: false,
    enrolleeId: enrollee.id
  })
  enrollee.participantTasks
    .push(taskForForm(studyEnvContext.currentEnv.configuredSurveys[0].survey, enrollee.id, false))

  const { RoutedComponent } = setupRouterTest(
    // eslint-disable-next-line @typescript-eslint/no-empty-function
    <LoadedEnrolleeView enrollee={enrollee} studyEnvContext={studyEnvContext} onUpdate={() => {}}/>)
  render(RoutedComponent)
  const surveyLinkContainer = screen.getByText('Survey number one').parentElement as HTMLElement
  // should show a badge
  expect(within(surveyLinkContainer).getByTitle('In Progress')).toBeInTheDocument()
})


test('renders consent links for configured consents', async () => {
  jest.spyOn(window, 'alert').mockImplementation(jest.fn())
  const studyEnvContext = mockStudyEnvContext()
  const enrollee = mockEnrollee()

  const { RoutedComponent } = setupRouterTest(
    // eslint-disable-next-line @typescript-eslint/no-empty-function
    <LoadedEnrolleeView enrollee={enrollee} studyEnvContext={studyEnvContext} onUpdate={() => {}}/>)
  render(RoutedComponent)
  const consentLinkContainer = screen.getByText('Mock consent').parentElement as HTMLElement
  // should have no badge since the enrollee hasn't completed the consent
  expect(consentLinkContainer.querySelector('svg')).toBeNull()
})

test('renders consent taken badges', async () => {
  jest.spyOn(window, 'alert').mockImplementation(jest.fn())
  const studyEnvContext = mockStudyEnvContext()
  const enrollee = mockEnrollee()
  enrollee.consentResponses.push({
    consentFormId: studyEnvContext.currentEnv.configuredConsents[0].consentFormId,
    resumeData: '',
    consented: true,
    completed: true,
    fullData: '',
    enrolleeId: enrollee.id
  })
  enrollee.participantTasks
    .push(taskForForm(studyEnvContext.currentEnv.configuredConsents[0].consentForm, enrollee.id, true))

  const { RoutedComponent } = setupRouterTest(
    // eslint-disable-next-line @typescript-eslint/no-empty-function
    <LoadedEnrolleeView enrollee={enrollee} studyEnvContext={studyEnvContext} onUpdate={() => {}}/>)
  render(RoutedComponent)
  const consentLinkContainer = screen.getByText('Mock consent').parentElement as HTMLElement
  // should show a completed checkmark
  expect(within(consentLinkContainer).getByTitle('Complete')).toBeInTheDocument()
})
