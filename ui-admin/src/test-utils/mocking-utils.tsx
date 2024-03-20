import { StudyEnvContextT, StudyEnvParams } from 'study/StudyEnvironmentRouter'
import {
  AdminTask,
  Answer,
  ConsentForm,
  DatasetDetails,
  Enrollee,
  EnrolleeSearchFacet,
  EnrolleeSearchResult,
  KitRequest,
  KitType,
  ParticipantNote,
  PepperKit,
  Portal,
  PortalStudy,
  SiteMediaMetadata,
  StudyEnvironment,
  StudyEnvironmentConsent,
  SurveyResponse,
  Trigger
} from 'api/api'
import {
  defaultSurvey,
  LocalizedEmailTemplate,
  ParticipantTask,
  ParticipantTaskStatus,
  ParticipantTaskType,
  Survey
} from '@juniper/ui-core'

import _times from 'lodash/times'
import _random from 'lodash/random'
import { EmailTemplate, StudyEnvironmentSurvey } from '@juniper/ui-core/build/types/study'
import { LoadedPortalContextT } from '../portal/PortalProvider'
import { PortalEnvironment } from '@juniper/ui-core/build/types/portal'
import { PortalEnvContext } from '../portal/PortalRouter'
import {
  EntityOptionsArrayFacet,
  EntityOptionsArrayFacetValue,
  EntityOptionsValue,
  FacetValue,
  StringOptionsFacet,
  StringOptionsFacetValue
} from '../api/enrolleeSearch'

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

/** mock with a mock portal and mock portalEnv */
export const mockPortalEnvContext: (envName: string) => PortalEnvContext = envName => ({
  portal: mockPortal(),
  updatePortal: jest.fn(),
  reloadPortal: () => Promise.resolve(mockPortal()),
  updatePortalEnv: jest.fn(),
  portalEnv: mockPortalEnvironment(envName)
})

/** returns simple mock portal environment */
export const mockPortalEnvironment: (envName: string) => PortalEnvironment = (envName: string) => ({
  portalEnvironmentConfig: {
    initialized: true,
    password: 'broad_institute',
    passwordProtected: false,
    acceptingRegistration: true
  },
  environmentName: envName,
  supportedLanguages: [
    { languageCode: 'en', languageName: 'English' },
    { languageCode: 'es', languageName: 'Spanish' }
  ]
})


/** returns a simple survey object for use/extension in tests */
export const mockSurvey: () => Survey = () => ({
  ...defaultSurvey,
  id: 'surveyId1',
  stableId: 'survey1',
  version: 1,
  content: '{}',
  name: 'Survey number one',
  lastUpdatedAt: 0,
  createdAt: 0,
  surveyType: 'RESEARCH'
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
export const mockStudyEnvContext: () => StudyEnvContextT = () => {
  const sandboxEnv: StudyEnvironment = {
    environmentName: 'sandbox',
    id: 'studyEnvId',
    configuredConsents: [mockConfiguredConsent()],
    configuredSurveys: [mockConfiguredSurvey()],
    triggers: [],
    studyEnvironmentConfig: {
      initialized: true,
      password: 'blah',
      passwordProtected: false,
      acceptingEnrollment: true
    }
  }
  return {
    study: {
      name: 'Fake study',
      studyEnvironments: [sandboxEnv],
      shortcode: 'fakeStudy'
    },
    portal: {
      shortcode: 'portalCode',
      id: 'portalId',
      portalStudies: [],
      portalEnvironments: [],
      name: 'Fake portal'
    },
    currentEnv: sandboxEnv,
    currentEnvPath: 'portalCode/studies/fakeStudy/env/sandbox'
  }
}

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
    studyEnvironmentId: 'studyEnvId1',
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

/** returns a mock PepperKitStatus */
export const mockExternalKitRequest = (): PepperKit => {
  return {
    kitId: '',
    currentStatus: 'Kit Without Label',
    labelDate: '',
    scanDate: '',
    receiveDate: '',
    trackingNumber: '',
    returnTrackingNumber: '',
    errorMessage: ''
  }
}

/** returns a mock kit request */
export const mockKitRequest: (args?: {
  enrolleeShortcode?: string,
  status?: string
}) => KitRequest = ({ enrolleeShortcode, status } = {}) => ({
  id: 'kitRequestId',
  createdAt: 1704393045,
  kitType: mockKitType(),
  status: status || 'CREATED',
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
  labeledAt: 1704393046,
  sentAt: 1704393046,
  trackingNumber: 'ABC123',
  details: '{"shippingId": "1234"}',
  enrolleeShortcode: enrolleeShortcode || 'JOSALK',
  skipAddressValidation: false
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

/** returns a mock enrollee task search facet */
export const mockTaskSearchFacet: () => EnrolleeSearchFacet = () => {
  const entities = [
    { value: 'consent', label: 'Consent' },
    { value: 'basicInfo', label: 'Basics' },
    { value: 'cardioHistory', label: 'Cardio History' }
  ]
  const options = [
    { value: 'COMPLETE', label: 'Complete' },
    { value: 'IN_PROGRESS', label: 'In progress' },
    { value: 'NEW', label: 'New' }
  ]

  return {
    keyName: 'status',
    category: 'participantTask',
    label: 'Task status',
    facetType: 'ENTITY_OPTIONS',
    entities,
    options
  }
}

/** returns a mock enrollee task search facet value */
export const mockTaskFacetValue: (facet: EntityOptionsArrayFacet, optionValue: string) =>
  FacetValue = (facet: EntityOptionsArrayFacet, optionValue: string) => {
    const optionValues: string[] = [optionValue]
    const facetValues = facet.entities.map(entity => new EntityOptionsValue(entity.value, optionValues))
    return new EntityOptionsArrayFacetValue(facet, { values: facetValues })
  }

/** returns a mock enrollee options search facet value */
export const mockOptionsFacetValue: (facet: StringOptionsFacet, optionValue: string) =>
  FacetValue = (facet: StringOptionsFacet, optionValue: string) => {
    const optionValues: string[] = [optionValue]
    return new StringOptionsFacetValue(facet, { values: optionValues })
  }

/** helper function to generate a ParticipantTask object for a survey and enrollee */
export const taskForForm = (form: Survey | ConsentForm, enrolleeId: string, taskType: ParticipantTaskType):
    ParticipantTask => {
  return {
    id: randomString(10),
    blocksHub: false,
    createdAt: 0,
    enrolleeId,
    portalParticipantUserId: randomString(10),
    status: 'NEW',
    studyEnvironmentId: randomString(10),
    taskType,
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
export const mockTrigger = (): Trigger => {
  return {
    id: 'noteId1',
    triggerType: 'EVENT',
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
    stableId: 'mock1',
    version: 1,
    defaultLanguage: 'en',
    localizedEmailTemplates: [mockLocalizedEmailTemplate()]
  }
}

/**
 *
 */
export const mockLocalizedEmailTemplate = (): LocalizedEmailTemplate => {
  return {
    language: 'en',
    subject: 'Mock subject',
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
/** mock siteMediaMetadata */
export const mockSiteMedia = (): SiteMediaMetadata => {
  return {
    id: 'image1',
    createdAt: Date.now(),
    cleanFileName: 'fileName.png',
    version: 1
  }
}

/** mock research survey task */
export const mockParticipantTask = (taskType: ParticipantTaskType, status: ParticipantTaskStatus): ParticipantTask => {
  return {
    id: randomId('task'),
    enrolleeId: randomId('enrollee'),
    portalParticipantUserId: randomId('ppUser'),
    targetName: 'Survey 1',
    targetStableId: 'researchSurvey1',
    targetAssignedVersion: 1,
    studyEnvironmentId: randomId('studyEnv'),
    createdAt: 0,
    lastUpdatedAt: 0,
    status,
    taskType,
    taskOrder: 0,
    blocksHub: true
  }
}

/**
 *
 */
export const mockStudyEnvParams = (): StudyEnvParams => {
  return {
    portalShortcode: 'foo',
    studyShortcode: 'bar',
    envName: 'sandbox'
  }
}

/** random ids to be used in place of guids */
export const randomId = (prefix: string): string => {
  return `${prefix}${Math.floor(Math.random() * 1000)}`
}
