import React from 'react'
import { render, screen } from '@testing-library/react'
import { RawEnrolleeSurveyView } from './EnrolleeSurveyView'
import {
  mockAnswer,
  mockConfiguredSurvey,
  mockEnrollee,
  mockStudyEnvContext,
  mockSurveyResponse
} from 'test-utils/mocking-utils'
import { setupRouterTest } from '@juniper/ui-core'

describe('RawEnrolleeSurveyView', () => {
  it('renders the survey version from the answers', async () => {
    const response = {
      ...mockSurveyResponse(),
      answers: [
        { ...mockAnswer(), surveyVersion: 2 }
      ]
    }
    const { RoutedComponent } = setupRouterTest(
      <RawEnrolleeSurveyView enrollee={mockEnrollee()}
        studyEnvContext={mockStudyEnvContext()}
        configSurvey={mockConfiguredSurvey()}
        onUpdate={jest.fn()}
        response={response}/>)
    render(RoutedComponent)
    expect(screen.getByText('(version 2)')).toBeInTheDocument()
  })

  it('renders the mutliple versions from the answers', async () => {
    const response = {
      ...mockSurveyResponse(),
      answers: [
        { ...mockAnswer(), surveyVersion: 2 },
        { ...mockAnswer(), surveyVersion: 3 }
      ]
    }
    const { RoutedComponent } = setupRouterTest(
      <RawEnrolleeSurveyView enrollee={mockEnrollee()}
        studyEnvContext={mockStudyEnvContext()}
        configSurvey={mockConfiguredSurvey()}
        onUpdate={jest.fn()}
        response={response}/>)
    render(RoutedComponent)
    expect(screen.getByText('(versions 2, 3)')).toBeInTheDocument()
  })
})
