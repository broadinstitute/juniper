import { Enrollee, ParticipantUser } from 'api/api'

/** gets a mock ParticipantUser */
export const mockParticipantUser: () => ParticipantUser = () => {
  return {
    id: 'user1',
    username: 'mockUser1@mock.com',
    token: 'fakeToken'
  }
}

/** gets a mock Enrollee */
export const mockEnrollee: () => Enrollee = () => {
  return {
    shortcode: 'AAABBB',
    consented: true,
    id: 'enrollee1',
    participantUserId: 'user1',
    profile: {
      sexAtBirth: 'female'
    },
    profileId: 'profile1',
    kitRequests: [],
    surveyResponses: [],
    consentResponses: [],
    preEnrollmentResponseId: undefined,
    participantTasks: [],
    createdAt: 0,
    lastUpdatedAt: 0,
    studyEnvironmentId: 'studyEnv1'
  }
}
