import {
  Enrollee,
  HubResponse,
  KitRequest,
  KitType,
  ParticipantTask,
  ParticipantTaskStatus,
  ParticipantTaskType,
  ParticipantUser,
  Profile
} from 'api/api'


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

/** mock enrollee profile */
export const mockProfile = (): Profile => {
  return {
    sexAtBirth: 'female'
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

/** mock a kit request */
export const mockKitRequest = (kitStatus: string, kitType: string): KitRequest => {
  const now = new Date().getTime() * 1000
  return {
    id: 'kitRequest1',
    kitType: mockKitType(kitType),
    createdAt: now,
    status: kitStatus,
    sentToAddress: '123 Main St',
    ...(['SENT', 'RECEIVED'].includes(kitStatus) && { sentAt: now }),
    ...(['RECEIVED'].includes(kitStatus) && { receivedAt: now })
  }
}

/** mock a kit type */
export const mockKitType = (kitType: string): KitType => {
  return {
    id: 'kitType1',
    name: kitType,
    displayName: (kitType == 'SALIVA' ? 'Saliva' : 'Blood'),
    description: `${kitType}  kit`
  }
}

/** mock hub response including no tasks and a mock enrollee */
export const mockHubResponse = (): HubResponse => {
  return {
    enrollee: mockEnrollee(),
    tasks: [],
    response: {},
    profile: mockProfile()
  }
}

/** random ids to be used in place of guids */
export const randomId = (prefix: string): string => {
  return `${prefix}${Math.floor(Math.random() * 1000)}`
}
