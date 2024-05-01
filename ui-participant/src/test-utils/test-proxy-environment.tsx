import { Portal } from '@juniper/ui-core'
import { Enrollee, EnrolleeRelation, ParticipantUser, PortalParticipantUser } from '../api/api'

export const mockPortal: Portal = {
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

export const mockUser: ParticipantUser = {
  id: 'test-jsalk-user',
  username: 'jsalk',
  token: ''
}

// 3 users, main proxy "Jonas Salk" with two dependents - "Peter Salk" and "Jonathan Salk"
export const mockPpUsersWithProxies: PortalParticipantUser[] = [
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

export const mockEnrolleesWithProxies: Enrollee[] = [
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
        enrolleeId: 'test-psalk-enrollee',
        portalParticipantUserId: 'test-psalk-pp-user',
        status: 'NEW',
        studyEnvironmentId: 'test-study-env-id',
        taskType: 'SURVEY',
        targetName: 'Demographics Survey',
        targetStableId: 'test-demographics-survey',
        targetAssignedVersion: 0,
        taskOrder: 0
      }
    ],
    participantUserId: 'test-psalk-user',
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
        targetName: 'Consent Survey',
        targetStableId: 'test-consent-survey',
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

export const mockRelations: EnrolleeRelation[] = [
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
