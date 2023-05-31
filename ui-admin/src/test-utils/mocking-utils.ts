import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import { DatasetDetails, Enrollee } from 'api/api'

// as we add more tests, we'll want to parameterize this and turn it into a proper factory
export const mockStudyEnvContext: () => StudyEnvContextT = () => ({
  study: { name: 'Fake study', studyEnvironments: [], shortcode: 'fakeStudy' },
  portal: { shortcode: 'portalCode', id: 'portalId', portalStudies: [], portalEnvironments: [], name: 'Fake portal' },
  currentEnv: {
    environmentName: 'sandbox',
    id: 'studyEnvId',
    configuredConsents: [],
    configuredSurveys: [],
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

export const mockDatasetDetails: (datasetName: string, status: string) => DatasetDetails =
    (datasetName: string, status: string) => ({
      createdAt: 1685557140,
      createdBy: 'study.admin@test.com',
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
export const mockEnrollee: () => Enrollee = () => ({
  shortcode: 'JOSALK',
  surveyResponses: [],
  consented: false,
  consentResponses: [],
  participantTasks: [],
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
  }
})
