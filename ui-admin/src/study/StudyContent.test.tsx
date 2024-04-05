import React from 'react'
import { fireEvent, render, screen, waitFor } from '@testing-library/react'

import StudyContent from './StudyContent'
import { mockConfiguredSurvey, mockStudyEnvContext, mockSurvey } from 'test-utils/mocking-utils'
import { setupRouterTest } from '../test-utils/router-testing-utils'
import Api from '../api/api'

test('renders surveys in-order', async () => {
  const studyEnvContext = mockStudyEnvContext()
  jest.spyOn(Api, 'findConfiguredSurveys').mockResolvedValue([
    {
      ...mockConfiguredSurvey(), surveyOrder: 2, studyEnvironmentId: studyEnvContext.currentEnv.id,
      survey: {
        ...mockSurvey(), name: 'Second survey'
      }
    },
    {
      ...mockConfiguredSurvey(), surveyOrder: 1, studyEnvironmentId: studyEnvContext.currentEnv.id,
      survey: {
        ...mockSurvey(), name: 'First survey'
      }
    }
  ])

  const { RoutedComponent } = setupRouterTest(<StudyContent studyEnvContext={studyEnvContext}/>)
  render(RoutedComponent)
  await waitFor(() => expect(screen.queryByTestId('loading-spinner')).not.toBeInTheDocument())
  const html = document.body.innerHTML
  const a = html.search('First survey')
  const b = html.search('Second survey')
  expect(a).toBeLessThan(b)
})

test('renders a Create Survey modal', async () => {
  const studyEnvContext = mockStudyEnvContext()
  jest.spyOn(Api, 'findConfiguredSurveys').mockResolvedValue([])
  const { RoutedComponent } = setupRouterTest(<StudyContent studyEnvContext={studyEnvContext}/>)
  render(RoutedComponent)
  await waitFor(() => expect(screen.queryByTestId('loading-spinner')).not.toBeInTheDocument())
  const addSurveyButton = screen.getByTestId('addResearchSurvey')
  fireEvent.click(addSurveyButton)
  expect(screen.getByText('Create new research')).toBeInTheDocument()
  expect(screen.getByText('Name')).toBeInTheDocument()
  expect(screen.getByText('Stable ID')).toBeInTheDocument()
})

test('renders a Create Outreach Survey modal', async () => {
  const studyEnvContext = mockStudyEnvContext()
  jest.spyOn(Api, 'findConfiguredSurveys').mockResolvedValue([])
  const { RoutedComponent } = setupRouterTest(<StudyContent studyEnvContext={studyEnvContext}/>)
  render(RoutedComponent)
  await waitFor(() => expect(screen.queryByTestId('loading-spinner')).not.toBeInTheDocument())
  const addSurveyButton = screen.getByTestId('addOutreachSurvey')
  fireEvent.click(addSurveyButton)
  expect(screen.getByText('Create new outreach')).toBeInTheDocument()
  expect(screen.getByText('Name')).toBeInTheDocument()
  expect(screen.getByText('Stable ID')).toBeInTheDocument()
})
