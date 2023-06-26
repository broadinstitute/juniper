import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import { DatasetDetails, Enrollee } from 'api/api'
import { Survey } from '@juniper/ui-core/build/types/forms'
import { ParticipantTask } from '@juniper/ui-core/build/types/task'

import _times from 'lodash/times'
import _random from 'lodash/random'
import { StudyEnvironmentSurvey } from '@juniper/ui-core/build/types/study'


// TODO: Add JSDoc
// eslint-disable-next-line jsdoc/require-jsdoc
export const mockSurvey: () => Survey = () => ({
  id: 'surveyId1',
  stableId: 'survey1',
  version: 1,
  content: '{}',
  name: 'Survey number one',
  lastUpdatedAt: 0,
  createdAt: 0
})

// as we add more tests, we'll want to parameterize this and turn it into a proper factory
// TODO: Add JSDoc
// eslint-disable-next-line jsdoc/require-jsdoc
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
    // TODO: Add JSDoc
    // eslint-disable-next-line jsdoc/require-jsdoc
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

// as we add more tests, we'll want to parameterize this and turn it into a proper factory
// TODO: Add JSDoc
// eslint-disable-next-line jsdoc/require-jsdoc
export const mockEnrollee: () => Enrollee = () => {
  const enrolleeId = randomString(10)
  return {
    id: enrolleeId,
    shortcode: 'JOSALK',
    surveyResponses: [],
    consented: false,
    consentResponses: [],
    profile: {
      givenName: 'Jonas',
      familyName: 'Salk',
      contactEmail: 'jsalk@test.com',
      birthDate: 0,
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
    kitRequests: [{
      id: 'kitRequestId',
      createdAt: 1,
      kitType: {
        id: 'kitTypeId',
        name: 'testKit',
        displayName: 'Test kit',
        description: 'Test sample collection kit'
      },
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
      status: 'CREATED'
    }]
  }
}

// TODO: Add JSDoc
// eslint-disable-next-line jsdoc/require-jsdoc
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

const randomString = (length: number) => {
  return _times(length, () => _random(35).toString(36)).join('')
}
