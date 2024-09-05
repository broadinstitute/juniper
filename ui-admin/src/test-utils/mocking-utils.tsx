import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import Api, {
  Answer,
  DatasetDetails,
  EnrolleeSearchExpressionResult,
  Notification,
  NotificationEventDetails,
  PepperKit,
  Portal, PortalEnvironmentConfig,
  PortalStudy,
  SiteMediaMetadata,
  StudyEnvironment,
  SurveyResponse,
  Trigger
} from 'api/api'
import {
  AlertTrigger,
  defaultSurvey,
  Enrollee,
  Family,
  KitRequest,
  KitType,
  LocalizedEmailTemplate,
  ParticipantDashboardAlert,
  ParticipantNote,
  ParticipantTask,
  ParticipantTaskStatus,
  ParticipantTaskType, renderWithRouter,
  StudyEnvParams,
  Survey,
  SurveyType,
  EmailTemplate,
  StudyEnvironmentSurvey,
  PortalEnvironment
} from '@juniper/ui-core'

import _times from 'lodash/times'
import _random from 'lodash/random'
import { LoadedPortalContextT, PortalContext, PortalContextT } from '../portal/PortalProvider'
import { PortalEnvContext } from '../portal/PortalRouter'
import React from 'react'
import { AdminUserContext } from '../providers/AdminUserProvider'
import { AdminUser } from '../api/adminUser'
import { mockAdminUser } from './user-mocking-utils'
import { UserContext } from '../user/UserProvider'
import { ReactNotifications } from 'react-notifications-component'

const randomString = (length: number) => {
  return _times(length, () => _random(35).toString(36)).join('')
}

/** returns a mock portal */
export const mockPortal = (): Portal => ({
  id: 'fakeportalid1',
  name: 'mock portal',
  shortcode: 'mock4u',
  portalStudies: [],
  portalEnvironments: [
    mockPortalEnvironment('sandbox')
  ]
})

/** mock portal with two supported languages */
export const mockTwoLanguagePortal = (): Portal => ({
  ...mockPortal(),
  portalEnvironments: [
    {
      ...mockPortalEnvironment('sandbox'),
      supportedLanguages: [MOCK_ENGLISH_LANGUAGE, MOCK_SPANISH_LANGUAGE]
    }
  ]
})

/** returns a simple portalContext, loosely modeled on OurHealth */
export const mockPortalContext = (): LoadedPortalContextT => ({
  portal: mockPortal(),
  updatePortal: jest.fn(),
  reloadPortal: () => Promise.resolve(mockPortal()),
  updatePortalEnv: jest.fn()
})

/** mock with a mock portal and mock portalEnv */
export const mockPortalEnvContext = (envName: string): PortalEnvContext => ({
  portal: mockPortal(),
  updatePortal: jest.fn(),
  reloadPortal: () => Promise.resolve(mockPortal()),
  updatePortalEnv: jest.fn(),
  portalEnv: mockPortalEnvironment(envName)
})

/** returns simple mock portal environment */
export const mockPortalEnvironment = (envName: string): PortalEnvironment => ({
  portalEnvironmentConfig: mockPortalEnvironmentConfig(),
  environmentName: envName,
  supportedLanguages: [
    { languageCode: 'en', languageName: 'English', id: '1' },
    { languageCode: 'es', languageName: 'Spanish', id: '2' }
  ],
  createdAt: 0
})

/**
 *
 */
export const mockPortalEnvironmentConfig = (): PortalEnvironmentConfig => {
  return {
    initialized: true,
    password: 'broad_institute',
    passwordProtected: false,
    acceptingRegistration: true,
    defaultLanguage: 'en'
  }
}


/** returns a simple survey object for use/extension in tests */
export const mockSurvey: (surveyType?: SurveyType) => Survey = (surveyType = 'RESEARCH') => ({
  ...defaultSurvey,
  id: 'surveyId1',
  stableId: 'survey1',
  version: 1,
  content: '{}',
  name: 'Survey number one',
  lastUpdatedAt: 0,
  createdAt: 0,
  surveyType
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
export const makeMockPortal = (name: string, portalStudies: PortalStudy[], shortcode: string) => {
  return {
    ...mockPortal(),
    name,
    portalStudies,
    shortcode
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
    configuredSurveys: [mockConfiguredSurvey()],
    triggers: [],
    studyEnvironmentConfig: {
      initialized: true,
      password: 'blah',
      passwordProtected: false,
      acceptingEnrollment: true,
      enableFamilyLinkage: false,
      acceptingProxyEnrollment: false,
      useDevDsmRealm: false,
      useStubDsm: false
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
    lastUpdatedAt: 0,
    shortcode: 'JOSALK',
    participantUserId: 'userId1',
    studyEnvironmentId: 'studyEnvId1',
    surveyResponses: [],
    consented: false,
    subject: true,
    consentResponses: [],
    participantNotes: [],
    profileId: 'profileId1',
    profile: {
      givenName: 'Jonas',
      familyName: 'Salk',
      contactEmail: 'jsalk@test.com',
      birthDate: [1994, 11, 20],
      doNotEmail: false,
      doNotEmailSolicit: false,
      preferredLanguage: 'en',
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

/**
 * Mocks most basic enrollee search expression result response.
 */
export const mockEnrolleeSearchExpressionResult: () => EnrolleeSearchExpressionResult = () => {
  return {
    enrollee: mockEnrollee(),
    profile: mockEnrollee().profile,
    families: [mockFamily(), mockFamily()]
  }
}

/**
 * Mocks a family object.
 */
export const mockFamily = (): Family => {
  return {
    id: 'familyId1',
    createdAt: 0,
    lastUpdatedAt: 0,
    shortcode: 'F_MOCK',
    members: [mockEnrollee()],
    studyEnvironmentId: 'studyEnvId1',
    relations: [],
    proband: mockEnrollee(),
    probandEnrolleeId: 'proband'
  }
}

/** helper function to generate a ParticipantTask object for a survey and enrollee */
export const taskForForm = (form: Survey, enrolleeId: string, taskType: ParticipantTaskType):
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
    actionType: 'NOTIFICATION',
    taskType: '',
    portalEnvironmentId: 'portalEnvId',
    studyEnvironmentId: 'studyEnvId',
    maxNumReminders: -1,
    afterMinutesIncomplete: -1,
    reminderIntervalMinutes: 10,
    updateTaskTargetStableId: '',
    active: true,
    emailTemplateId: 'emailTemplateId',
    emailTemplate: mockEmailTemplate(),
    rule: ''
  }
}

/**
 * Returns a mock Notification
 */
export const mockNotification = (): Notification => {
  return {
    id: 'notificationId1',
    triggerId: 'triggerId',
    createdAt: 0,
    lastUpdatedAt: 0,
    deliveryStatus: 'SENT',
    deliveryType: 'EMAIL',
    sentTo: 'jsalk@test.com',
    retries: 0
  }
}

/**
 * Returns a mock NotificationEventDetails
 */
export const mockEventDetails = (): NotificationEventDetails => {
  return {
    subject: 'This is a test email',
    toEmail: 'jsalk@test.com',
    fromEmail: 'info@juniper.terra.bio',
    status: 'DELIVERED',
    opensCount: 0,
    clicksCount: 0,
    lastEventTime: 0
  }
}

/** Mock EmailTemplate */
export const mockEmailTemplate = (): EmailTemplate => {
  return {
    id: 'emailTemplate1',
    name: 'Mock template',
    stableId: 'mock1',
    version: 1,
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
export const mockAdminTask = (): ParticipantTask => {
  return {
    id: 'taskId1',
    blocksHub: false,
    enrolleeId: 'enrolleeId1',
    portalParticipantUserId: 'ppUserId1',
    assignedAdminUserId: 'userId1',
    createdAt: 0,
    taskOrder: 1,
    taskType: 'ADMIN_FORM',
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

/** mock ParticipantDashboardAlert */
export const mockDashboardAlert = (title: string, detail: string, trigger: AlertTrigger): ParticipantDashboardAlert => {
  return {
    id: randomId('alert'),
    title,
    detail,
    alertType: 'INFO',
    trigger
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

/** APIs invoked for query builder UX */
export const mockExpressionApis = () => {
  jest.spyOn(window, 'alert').mockImplementation(jest.fn())
  jest.spyOn(Api, 'executeSearchExpression').mockResolvedValue([])
  jest.spyOn(Api, 'getExpressionSearchFacets').mockResolvedValue({})
}

export const MOCK_ENGLISH_LANGUAGE = {
  languageCode: 'en',
  languageName: 'English',
  id: '1'
}
export const MOCK_SPANISH_LANGUAGE = {
  languageCode: 'es',
  languageName: 'Español',
  id: '1'
}

export type RenderInPortalRouterOpts = {
  envName?: string,
  mockWindowAlert?: boolean,
  adminUsers?: AdminUser[], // will be put in the AdminUserContext
  user: AdminUser, // will be put in the UserContext
  permissions?: string[] // convenience for setting permissions without setting a full user with portalPermissions
}

export const defaultRenderOpts = {
  envName: 'sandbox',
  mockWindowAlert: true,
  adminUsers: [],
  user: mockAdminUser(false),
  permissions: undefined
}

/**
 * renders the children in a PortalProvider context and simulating appropriate routes
 * so that useStudyEnvParams hook works as expected. Hardcoded to sandbox for now.
 * Also includes AdminUserContext for showing admin user names
 *
 * By default, this mocks window alert since many of our contexts use window alert for error handling.
 * */
export const renderInPortalRouter = (portal: Portal,
  children: React.ReactNode, opts: RenderInPortalRouterOpts = defaultRenderOpts) => {
  const portalContext: PortalContextT = {
    ...mockPortalContext(),
    portal,
    isLoading: false,
    isError: false
  }

  if (opts.mockWindowAlert) {
    jest.spyOn(window, 'alert').mockImplementation(jest.fn())
  }

  if (opts.permissions) {
    opts.user.portalPermissions = { [portal.id]: opts.permissions }
  }

  const studyShortcode = portal.portalStudies[0] ? portal.portalStudies[0].study.shortcode : 'fakestudy'
  return renderWithRouter(
    <AdminUserContext.Provider value={{ users: opts.adminUsers ?? [], isLoading: false }}>
      <UserContext.Provider
        value={{ user: opts.user, logoutUser: jest.fn(), loginUser: jest.fn(), loginUserUnauthed: jest.fn() }}>
        <PortalContext.Provider value={portalContext}>
          { children }
        </PortalContext.Provider>
        <ReactNotifications/>
      </UserContext.Provider>
    </AdminUserContext.Provider>, [`/${portal.shortcode}/studies/${studyShortcode}/${opts.envName}`],
    ':portalShortcode/studies/:studyShortcode/:studyEnv')
}
