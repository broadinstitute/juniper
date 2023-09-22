import React from 'react'

import EnrolleeView from './EnrolleeView'
import { setupRouterTest } from 'test-utils/router-testing-utils'
import { mockEnrollee, mockStudyEnvContext, taskForForm } from 'test-utils/mocking-utils'
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
  enrollee.participantTasks
    .push(taskForForm(studyEnvContext.currentEnv.configuredSurveys[0].survey, enrollee.id, false))

  const { RoutedComponent } = setupRouterTest(
    // eslint-disable-next-line @typescript-eslint/no-empty-function
    <EnrolleeView enrollee={enrollee} studyEnvContext={studyEnvContext} onUpdate={() => {}}/>)
  render(RoutedComponent)
  const surveyLink = screen.getByText('Survey number one')
  // should show a badge
  expect(surveyLink.querySelector('span')).toHaveTextContent('1')
})


test('renders consent links for configured consents', async () => {
  const studyEnvContext = mockStudyEnvContext()
  const enrollee = mockEnrollee()

  const { RoutedComponent } = setupRouterTest(
    // eslint-disable-next-line @typescript-eslint/no-empty-function
    <EnrolleeView enrollee={enrollee} studyEnvContext={studyEnvContext} onUpdate={() => {}}/>)
  render(RoutedComponent)
  const surveyLink = screen.getByText('Mock consent')
  // should have no badge since the enrollee hasn't completed the consent
  expect(surveyLink.querySelector('span')).toBeNull()
})

test('renders consent taken badges', async () => {
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
    <EnrolleeView enrollee={enrollee} studyEnvContext={studyEnvContext} onUpdate={() => {}}/>)
  render(RoutedComponent)
  const consentLink = screen.getByText('Mock consent')
  // should show a completed checkmark
  expect(consentLink.querySelector('title')?.textContent).toEqual('completed')
})
