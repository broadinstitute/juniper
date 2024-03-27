import { render, screen, waitFor } from '@testing-library/react'
import React from 'react'
import HubPage from './HubPage'
import { setupRouterTest } from 'test-utils/router-testing-utils'
import { MockI18nProvider, mockTextsDefault } from '@juniper/ui-core'
import { mockParticipantTask, mockSurvey } from '../test-utils/test-participant-factory'
import Api from '../api/api'

jest.mock('../providers/PortalProvider', () => {
  return {
    usePortalEnv: () => ({
      portal: {
        id: 'portal-id',
        name: 'Test Portal',
        shortcode: 'TESTPORTAL',
        portalEnvironments: [],
        portalStudies: [{
          study: {
            name: 'Test Study',
            shortcode: 'STUDYSHORTCODE',
            studyEnvironments: [{
              id: 'test-study-env-id',
              studyEnvironmentConfig: {
                acceptingEnrollment: true
              }
            }]
          }
        }]
      }
    })
  }
})

jest.mock('../providers/UserProvider', () => {
  return {
    useUser: () => ({
      enrollees: [{
        id: 'enrollee-id',
        shortcode: 'ENSHORTCODE',
        consented: false,
        studyEnvironmentId: 'test-study-env-id',
        participantTasks: [{
          id: 'task-id',
          blocksHub: true,
          createdAt: 0,
          enrolleeId: 'enrollee-id',
          portalParticipantUserId: 'portal-participant-user-id',
          status: 'NEW',
          studyEnvironmentId: 'test-study-env-id',
          taskType: 'CONSENT',
          targetName: 'Test Survey',
          targetStableId: 'test_survey_id',
          targetAssignedVersion: 0,
          taskOrder: 0
        }],
        kitRequests: []
      }]
    })
  }
})

describe('HubPage', () => {
  it('is rendered with the study name', () => {
    const { RoutedComponent } = setupRouterTest(
      <MockI18nProvider>
        <HubPage/>
      </MockI18nProvider>)
    render(RoutedComponent)

    expect(screen.getByText('Test Study')).toBeInTheDocument()
  })

  it('is rendered with a Start button for the next new task', async () => {
    const mockTasks = {
      surveyTasks: [],
      consentTasks: [
        {
          task: mockParticipantTask('CONSENT', 'NEW'),
          form: mockSurvey('test_consent')
        }
      ],
      outreachTasks: []
    }
    jest.spyOn(Api, 'listTasksWithSurveys').mockResolvedValue(mockTasks)

    const { RoutedComponent } = setupRouterTest(
      <MockI18nProvider mockTexts={mockTextsDefault}>
        <HubPage/>
      </MockI18nProvider>)
    render(RoutedComponent)

    const startConsent = await waitFor(() => screen.getByText('Start Consent'))
    expect(startConsent).toBeInTheDocument()
  })
})
