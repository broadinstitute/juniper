import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import { DatasetDetails, Enrollee, KitRequest, KitType, ParticipantNote, Portal } from 'api/api'
import { Survey } from '@juniper/ui-core/build/types/forms'
import { ParticipantTask } from '@juniper/ui-core/build/types/task'

import _times from 'lodash/times'
import _random from 'lodash/random'
import { StudyEnvironmentSurvey } from '@juniper/ui-core/build/types/study'
import { LoadedPortalContextT } from '../portal/PortalProvider'
import { PortalEnvironment } from '@juniper/ui-core/build/types/portal'

const randomString = (length: number) => {
  return _times(length, () => _random(35).toString(36)).join('')
}

/** returns a mock portal */
export const mockPortal: () => Portal = () => ({
  id: 'fakeportalid1',
  name: 'mock portal',
  shortcode: 'mock4u',
  portalStudies: [],
  portalEnvironments: [
    mockPortalEnvironment('sandbox')
  ]
})

/** returns a simple portalContext, loosely modeled on OurHealth */
export const mockPortalContext: () => LoadedPortalContextT = () => ({
  portal: mockPortal(),
  updatePortal: jest.fn(),
  reloadPortal: () => Promise.resolve(mockPortal()),
  updatePortalEnv: jest.fn(),
  isError: false,
  isLoading: false
})

/** returns simple mock portal environment */
export const mockPortalEnvironment: (envName: string) => PortalEnvironment = (envName: string) => ({
  portalEnvironmentConfig: {
    initialized: true,
    password: 'broad_institute',
    passwordProtected: false,
    acceptingRegistration: true
  },
  environmentName: envName
})


/** returns a simple survey object for use/extension in tests */
export const mockSurvey: () => Survey = () => ({
  id: 'surveyId1',
  stableId: 'survey1',
  version: 1,
  content: '{}',
  name: 'Survey number one',
  lastUpdatedAt: 0,
  createdAt: 0
})

/** returns a simple studyEnvContext object for use/extension in tests */
export const mockStudyEnvContext: () => StudyEnvContextT = () => ({
  study: { name: 'Fake study', studyEnvironments: [], shortcode: 'fakeStudy' },
  portal: { shortcode: 'portalCode', id: 'portalId', portalStudies: [], portalEnvironments: [], name: 'Fake portal' },
  currentEnv: {
    environmentName: 'sandbox',
    id: 'studyEnvId',
    configuredConsents: [],
    configuredSurveys: [mockConfiguredSurvey()],
    notificationConfigs: [],
    studyEnvironmentConfig: {
      initialized: true,
      password: '',
      passwordProtected: false,
      acceptingEnrollment: true
    }
  },
  currentEnvPath: 'portalCode/studies/fakeStudy/env/sandbox'
})

/**
 *
 */
export const mockConfiguredSurvey: () => StudyEnvironmentSurvey = () => {
  return {
    id: 'fakeGuid',
    surveyId: 'surveyId1',
    surveyOrder: 1,
    required: false,
    recur: false,
    recurrenceIntervalDays: 0,
    allowAdminEdit: true,
    allowParticipantStart: true,
    allowParticipantReedit: true,
    prepopulate: true,
    survey: mockSurvey()
  }
}

export const mockDatasetDetails: (datasetName: string, status: string) => DatasetDetails =
  /** returns mock dataset for use/extension in tests */
  (datasetName: string, status: string) => ({
    createdAt: 1685557140,
    createdBy: '0b9ade05-f7e3-483e-b85a-43deac7505c0',
    datasetName,
    description: 'a successfully created dataset',
    id: 'a-successful-id',
    lastExported: 0,
    lastUpdatedAt: 0,
    status,
    studyEnvironmentId: 'studyEnvId',
    tdrDatasetId: 'a-fake-tdr-dataset-id'
  })

/** returns a mock kit request type */
export const mockKitType: () => KitType = () => ({
  id: 'kitTypeId',
  name: 'testKit',
  displayName: 'Test kit',
  description: 'Test sample collection kit'
})

/** returns a mock kit request */
export const mockKitRequest: (args?: {
  enrollee?: Enrollee,
  dsmStatus?: string
}) => KitRequest = ({ enrollee, dsmStatus } = {}) => ({
  id: 'kitRequestId',
  createdAt: 1,
  enrollee,
  kitType: mockKitType(),
  // This is intentionally a little different from the enrollee's current mailing address to show that sentToAddress
  // is a capture of the mailing address at the time the kit was sent.
  sentToAddress: JSON.stringify({
    firstName: 'Jonas',
    lastName: 'Salk',
    street1: '1234 Fake Street',
    city: 'Cambridge',
    state: 'MA',
    postalCode: '02138',
    country: 'US'
  }),
  status: 'CREATED',
  dsmStatus,
  pepperStatus: {
    kitId: '',
    currentStatus: '(unknown)',
    labelDate: '',
    scanDate: '',
    receiveDate: '',
    trackingNumber: '',
    returnTrackingNumber: ''
  }
})

/** returns a simple mock enrollee loosely based on the jsalk.json synthetic enrollee */
export const mockEnrollee: () => Enrollee = () => {
  const enrolleeId = randomString(10)
  return {
    id: enrolleeId,
    createdAt: 0,
    shortcode: 'JOSALK',
    participantUserId: 'userId1',
    surveyResponses: [],
    consented: false,
    consentResponses: [],
    participantNotes: [],
    profile: {
      givenName: 'Jonas',
      familyName: 'Salk',
      contactEmail: 'jsalk@test.com',
      birthDate: [1994, 11, 20],
      doNotEmail: false,
      doNotEmailSolicit: false,
      phoneNumber: '555.1212',
      mailingAddress: {
        street1: '123 fake street',
        street2: '',
        state: 'MA',
        city: 'Cambridge',
        postalCode: '02138',
        country: 'US'
      }
    },
    participantTasks: [],
    kitRequests: [mockKitRequest()]
  }
}

/** helper function to generate a ParticipantTask object for a survey and enrollee */
export const taskForSurvey = (survey: Survey, enrolleeId: string): ParticipantTask => {
  return {
    id: randomString(10),
    blocksHub: false,
    createdAt: 0,
    enrolleeId,
    portalParticipantUserId: randomString(10),
    status: 'NEW',
    studyEnvironmentId: randomString(10),
    taskType: 'SURVEY',
    targetName: survey.name,
    targetStableId: survey.stableId,
    targetAssignedVersion: survey.version,
    taskOrder: 1
  }
}

/** mock ParticipantNote */
export const mockParticipantNote = (): ParticipantNote => {
  return {
    id: 'noteId1',
    creatingAdminUserId: 'adminId',
    enrolleeId: 'enrolleeId',
    createdAt: 0,
    lastUpdatedAt: 0,
    text: 'some note text'
  }
}
