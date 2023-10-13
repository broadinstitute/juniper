import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import {
  AdminTask,
  Answer,
  ConsentForm,
  DatasetDetails,
  Enrollee,
  EnrolleeSearchResult,
  KitRequest,
  KitType,
  NotificationConfig,
  ParticipantNote,
  Portal,
  PortalStudy,
  StudyEnvironmentConsent, SurveyResponse
} from 'api/api'
import { Survey } from '@juniper/ui-core/build/types/forms'
import { ParticipantTask } from '@juniper/ui-core/build/types/task'

import _times from 'lodash/times'
import _random from 'lodash/random'
import { EmailTemplate, StudyEnvironmentSurvey } from '@juniper/ui-core/build/types/study'
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

/** returns a mock portal study */
export const makeMockPortalStudy = (name: string, shortcode: string): PortalStudy => {
  return {
    study: {
      name,
      shortcode,
      studyEnvironments: []
    }
  }
}

/** returns a mock portal with the specified studies */
export const makeMockPortal = (name: string, portalStudies: PortalStudy[]) => {
  return {
    ...mockPortal(),
    name,
    portalStudies
  }
}

/** returns a list of survey versions */
export const mockSurveyVersionsList: () => Survey[] = () => ([
  {
    ...mockSurvey(),
    id: 'surveyId1',
    stableId: 'survey1',
    version: 1
  },
  {
    ...mockSurvey(),
    id: 'surveyId2',
    stableId: 'survey1',
    version: 2
  }
])

/** returns a simple studyEnvContext object for use/extension in tests */
export const mockStudyEnvContext: () => StudyEnvContextT = () => ({
  study: { name: 'Fake study', studyEnvironments: [], shortcode: 'fakeStudy' },
  portal: { shortcode: 'portalCode', id: 'portalId', portalStudies: [], portalEnvironments: [], name: 'Fake portal' },
  currentEnv: {
    environmentName: 'sandbox',
    id: 'studyEnvId',
    configuredConsents: [mockConfiguredConsent()],
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

/** Mock StudyEnvironmentConsent */
export const mockConfiguredConsent = (): StudyEnvironmentConsent => {
  return {
    id: 'fakeGuid',
    consentFormId: 'consentId1',
    consentOrder: 1,
    consentForm: mockConsentForm(),
    allowAdminEdit: false,
    allowParticipantReedit: false,
    allowParticipantStart: true,
    prepopulate: false
  }
}

/** fake ConsentForm */
export const mockConsentForm = (): ConsentForm => {
  return {
    id: 'fakeGuid2',
    content: '{"pages": []}',
    stableId: 'form1',
    version: 1,
    name: 'Mock consent',
    createdAt: 0,
    lastUpdatedAt: 0
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
    currentStatus: 'Kit Without Label',
    labelDate: '',
    scanDate: '',
    receiveDate: '',
    trackingNumber: '',
    returnTrackingNumber: '',
    errorMessage: ''
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

/** returns a mock enrollee search result */
export const mockEnrolleeSearchResult: () => EnrolleeSearchResult = () => {
  return {
    enrollee: mockEnrollee(),
    profile: mockEnrollee().profile,
    mostRecentKitStatus: null,
    participantUser: {
      lastLogin: 50405345,
      username: `${randomString(10)}@test.com`
    }
  }
}

/** helper function to generate a ParticipantTask object for a survey and enrollee */
export const taskForForm = (form: Survey | ConsentForm, enrolleeId: string,
  isConsent: boolean): ParticipantTask => {
  return {
    id: randomString(10),
    blocksHub: false,
    createdAt: 0,
    enrolleeId,
    portalParticipantUserId: randomString(10),
    status: 'NEW',
    studyEnvironmentId: randomString(10),
    taskType: isConsent ? 'CONSENT' : 'SURVEY',
    targetName: form.name,
    targetStableId: form.stableId,
    targetAssignedVersion: form.version,
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

/** mock NotificationConfig */
export const mockNotificationConfig = (): NotificationConfig => {
  return {
    id: 'noteId1',
    notificationType: 'EVENT',
    eventType: 'CONSENT',
    deliveryType: 'EMAIL',
    taskType: '',
    portalEnvironmentId: 'portalEnvId',
    studyEnvironmentId: 'studyEnvId',
    maxNumReminders: -1,
    afterMinutesIncomplete: -1,
    reminderIntervalMinutes: 10,
    taskTargetStableId: '',
    active: true,
    emailTemplateId: 'emailTemplateId',
    emailTemplate: mockEmailTemplate(),
    rule: ''
  }
}

/** Mock EmailTemplate */
export const mockEmailTemplate = (): EmailTemplate => {
  return {
    id: 'emailTemplate1',
    name: 'Mock template',
    subject: 'Mock subject',
    stableId: 'mock1',
    version: 1,
    body: 'Mock email message'
  }
}


/** mock admin task */
export const mockAdminTask = (): AdminTask => {
  return {
    id: 'taskId1',
    assignedAdminUserId: 'userId1',
    createdAt: 0,
    status: 'NEW',
    creatingAdminUserId: 'userId2',
    studyEnvironmentId: 'studyEnvId1'
  }
}

/** mock response */
export const mockSurveyResponse = (): SurveyResponse => {
  return {
    id: 'responseId1',
    surveyId: 'survey1',
    resumeData: '{}',
    enrolleeId: 'enrollee1',
    complete: false,
    answers: []
  }
}

/** mock survey answer */
export const mockAnswer = (): Answer => {
  return {
    surveyVersion: 1,
    stringValue: 'foo',
    questionStableId: 'question1'
  }
}
