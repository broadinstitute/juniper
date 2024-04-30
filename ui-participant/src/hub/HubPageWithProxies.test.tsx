import { render, screen, waitFor } from '@testing-library/react'
import React from 'react'
import HubPage from './HubPage'
import { setupRouterTest } from 'test-utils/router-testing-utils'
import { MockI18nProvider, Portal } from '@juniper/ui-core'
import ProvideFullTestUserContext from 'test-utils/ProvideFullTestUserContext'
import { Enrollee, EnrolleeRelation, ParticipantUser, PortalParticipantUser } from '../api/api'

const portal: Portal = {
  id: 'portal-id',
  name: 'Test Portal',
  shortcode: 'TESTPORTAL',
  portalEnvironments: [
    {
      environmentName: 'sandbox',
      portalEnvironmentConfig: {
        initialized: true,
        acceptingRegistration: false,
        passwordProtected: false,
        password: '',
        defaultLanguage: ''
      },
      supportedLanguages: [],
      siteContent: {
        id: '',
        createdAt: 0,
        stableId: '',
        version: 0,
        localizedSiteContents: [
          {
            primaryBrandColor: 'blue',
            language: '',
            landingPage: {
              title: '',
              path: '',
              sections: []
            },
            navbarItems: [],
            navLogoCleanFileName: '',
            navLogoVersion: 0
          }
        ]
      }
    }
  ],
  portalStudies: [{
    study: {
      name: 'Test Study',
      shortcode: 'STUDYSHORTCODE',
      studyEnvironments: [{
        id: 'test-study-env-id',
        studyEnvironmentConfig: {
          acceptingEnrollment: true,
          acceptingProxyEnrollment: true,
          initialized: true,
          passwordProtected: false,
          password: ''
        },
        environmentName: 'sandbox',
        configuredConsents: [],
        configuredSurveys: [],
        triggers: []
      }]
    }
  }]
}

const mockUser: ParticipantUser = {
  id: 'test-jsalk-user',
  username: 'jsalk',
  token: ''
}

// 3 users, main proxy "Jonas Salk" with two dependents - "Peter Salk" and "Jonathan Salk"
const mockPpUsersWithProxies: PortalParticipantUser[] = [
  {
    id: 'test-jsalk-pp-user',
    participantUserId: 'test-jsalk-user',
    profileId: 'test-jsalk-profile',
    profile: {}
  },
  {
    id: 'test-psalk-pp-user',
    participantUserId: 'test-psalk-user',
    profileId: 'test-psalk-profile',
    profile: {}
  },
  {
    id: 'test-jtsalk-pp-user',
    participantUserId: 'test-jtsalk-user',
    profileId: 'test-jtsalk-profile',
    profile: {}
  }
]

const mockEnrolleesWithProxies: Enrollee[] = [
  {
    id: 'test-jsalk-enrollee',
    profileId: 'test-jsalk-profile',
    profile: {
      id: 'test-jsalk-profile',
      givenName: 'Jonas',
      familyName: 'Salk',
      birthDate: [1987, 11, 12],
      mailingAddress: {
        street1: '415 Main St',
        street2: '',
        city: 'Cambridge',
        state: 'MA',
        country: 'US',
        postalCode: '02119'
      },
      doNotEmailSolicit: false,
      phoneNumber: '123-456-7890',
      contactEmail: 'jsalk@test.com',
      doNotEmail: false,
      sexAtBirth: 'M'
    },
    consented: true,
    subject: false,
    consentResponses: [],
    createdAt: 0,
    kitRequests: [],
    lastUpdatedAt: 0,
    participantTasks: [],
    participantUserId: 'test-jsalk-user',
    shortcode: 'JSALK',
    studyEnvironmentId: 'test-study-env-id',
    surveyResponses: []
  },
  {
    id: 'test-psalk-enrollee',
    profileId: 'test-psalk-profile',
    profile: {
      id: 'test-psalk-profile',
      givenName: 'Peter',
      familyName: 'Salk',
      birthDate: [1987, 11, 12],
      mailingAddress: {
        street1: '415 Main St',
        street2: '',
        city: 'Cambridge',
        state: 'MA',
        country: 'US',
        postalCode: '02119'
      },
      doNotEmailSolicit: false,
      phoneNumber: '123-456-7890',
      contactEmail: 'psalk@test.com',
      doNotEmail: false,
      sexAtBirth: 'M'
    },
    consented: true,
    subject: false,
    consentResponses: [],
    createdAt: 0,
    kitRequests: [],
    lastUpdatedAt: 0,
    participantTasks: [
      {
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
      }
    ],
    participantUserId: 'test-jsalk-user',
    shortcode: 'PSALK',
    studyEnvironmentId: 'test-study-env-id',
    surveyResponses: []
  },
  {
    id: 'test-jtsalk-enrollee',
    profileId: 'test-jtsalk-profile',
    profile: {
      id: 'test-jtsalk-profile',
      givenName: 'Jonathan',
      familyName: 'Salk',
      birthDate: [1987, 11, 12],
      mailingAddress: {
        street1: '415 Main St',
        street2: '',
        city: 'Cambridge',
        state: 'MA',
        country: 'US',
        postalCode: '02119'
      },
      doNotEmailSolicit: false,
      phoneNumber: '123-456-7890',
      contactEmail: 'jtsalk@test.com',
      doNotEmail: false,
      sexAtBirth: 'M'
    },
    consented: true,
    subject: false,
    consentResponses: [],
    createdAt: 0,
    kitRequests: [],
    lastUpdatedAt: 0,
    participantTasks: [
      {
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
      }
    ],
    participantUserId: 'test-jtsalk-user',
    shortcode: 'JTSALK',
    studyEnvironmentId: 'test-study-env-id',
    surveyResponses: []
  }
]

const mockRelations: EnrolleeRelation[] = [
  {
    id: 'test-jsalk-psalk-relation',
    relationshipType: 'PROXY',
    targetEnrolleeId: 'test-psalk-enrollee',
    createdAt: 0,
    lastUpdatedAt: 0,
    participantUserId: 'test-jsalk-user'
  },
  {
    id: 'test-jsalk-jtsalk-relation',
    relationshipType: 'PROXY',
    targetEnrolleeId: 'test-jtsalk-enrollee',
    createdAt: 0,
    lastUpdatedAt: 0,
    participantUserId: 'test-jsalk-user'
  }
]

jest.mock('../api/api', () => {
  return {
    getPortalEnvDashboardAlerts: () => Promise.resolve([]),
    getPortal: () => Promise.resolve(portal),
    listOutreachActivities: () => Promise.resolve([])
  }
})


describe('HubPage with proxies', () => {
  it('is rendered with governed user and study name', async () => {
    const { RoutedComponent } = setupRouterTest(
      <ProvideFullTestUserContext
        enrollees={mockEnrolleesWithProxies}
        ppUsers={mockPpUsersWithProxies}
        relations={mockRelations}
        user={mockUser}
        portal={portal}
        activePpUserId={'test-psalk-pp-user'}
      >
        <MockI18nProvider>
          <HubPage/>
        </MockI18nProvider>
      </ProvideFullTestUserContext>
    )
    render(RoutedComponent)

    await waitFor(() => expect(screen.getByText('Test Study')).toBeInTheDocument())
    await waitFor(() => expect(screen.getByText('Peter Salk')).toBeInTheDocument())
  })
})
