import React from 'react'
import { render } from '@testing-library/react'

import StudyContent from './StudyContent'
import { mockConfiguredSurvey, mockStudyEnvContext, mockSurvey } from 'test-utils/mocking-utils'
import { setupRouterTest } from '../test-utils/router-testing-utils'

test('renders surveys in-order', async () => {
  const studyEnvContext = mockStudyEnvContext()
  studyEnvContext.currentEnv.configuredSurveys = [
    {
      ...mockConfiguredSurvey(), surveyOrder: 2,
      survey: {
        ...mockSurvey(), name: 'Second survey'
      }
    },
    {
      ...mockConfiguredSurvey(), surveyOrder: 1,
      survey: {
        ...mockSurvey(), name: 'First survey'
      }
    }
  ]

  const { RoutedComponent } = setupRouterTest(<StudyContent studyEnvContext={studyEnvContext}/>)
  render(RoutedComponent)
  const html = document.body.innerHTML
  const a = html.search('First survey')
  const b = html.search('Second survey')
  expect(a).toBeLessThan(b)
})
