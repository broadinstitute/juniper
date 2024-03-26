import { render, screen } from '@testing-library/react'
import React from 'react'
import HubPage from './HubPage'
import { setupRouterTest } from 'test-utils/router-testing-utils'
import { MockI18nProvider, mockTextsDefault } from '@juniper/ui-core'

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
      relations: [],
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
      }],
      activeEnrollee: {
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
      },
      activeEnrolleeProfile: {
        givenName: 'Test',
        givenFamily: 'User'
      }
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

  it('is rendered with a Start button for the next new task', () => {
    const { RoutedComponent } = setupRouterTest(
      <MockI18nProvider mockTexts={mockTextsDefault}>
        <HubPage />
      </MockI18nProvider>)
    render(RoutedComponent)

    expect(screen.getByText('Start Consent')).toBeInTheDocument()
  })
})
