import {
  KitType,
  ParticipantTask,
  ParticipantTaskStatus,
  ParticipantTaskType, PortalParticipantUser,
  Survey
} from 'api/api'
import {
  defaultSurvey,
  Enrollee,
  HubResponse, KitRequest, KitRequestStatus,
  ParticipantUser,
  Profile
} from '@juniper/ui-core'


/** gets a mock ParticipantUser */
export const mockParticipantUser: () => ParticipantUser = () => {
  return {
    id: 'user1',
    username: 'mockUser1@mock.com',
    shortcode: 'ACC_fakeShortcode',
    token: 'fakeToken',
    lastLogin: 0,
    createdAt: 0
  }
}

export const mockPortalParticipantUser = (): PortalParticipantUser => {
  return {
    profile: mockProfile(),
    profileId: 'profile1',
    id: 'ppUser1',
    participantUserId: 'user1'
  }
}

/** gets a mock Enrollee */
export const mockEnrollee: () => Enrollee = () => {
  return {
    shortcode: 'AAABBB',
    consented: true,
    subject: true,
    id: 'enrollee1',
    participantUserId: 'user1',
    profile: {
      sexAtBirth: 'female'
    },
    profileId: 'profile1',
    kitRequests: [],
    surveyResponses: [],
    consentResponses: [],
    preRegResponse: undefined,
    preEnrollmentResponse: undefined,
    participantTasks: [],
    participantNotes: [],
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

/**
 * mock survey form
 */
export const mockSurvey = (stableId: string): Survey => {
  return {
    ...defaultSurvey,
    id: 'survey1',
    createdAt: 0,
    lastUpdatedAt: 0,
    name: 'Survey 1',
    content: '{}',
    stableId,
    version: 1,
    surveyType: 'RESEARCH',
    blurb: 'This is a survey'
  }
}

/** mock a kit request */
export const mockKitRequest = (kitStatus: KitRequestStatus, kitType: string): KitRequest => {
  const now = new Date().getTime() * 1000
  return {
    id: 'kitRequest1',
    kitType: mockKitType(kitType),
    distributionMethod: 'MAILED',
    skipAddressValidation: false,
    createdAt: now,
    status: kitStatus,
    sentToAddress: '123 Main St',
    ...(['SENT', 'RECEIVED'].includes(kitStatus) && { sentAt: now }),
    ...(['RECEIVED'].includes(kitStatus) && { receivedAt: now })
  }
}

export const mockAssignedKitRequest = (kitStatus: KitRequestStatus, kitType: string): KitRequest => {
  return {
    ...mockKitRequest(kitStatus, kitType),
    distributionMethod: 'IN_PERSON',
    kitLabel: 'assigned-label',
    returnTrackingNumber: 'some-tracking'
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
    response: {
      resumeData: '',
      enrolleeId: 'enrollee1',
      surveyId: 'survey1',
      complete: true,
      answers: []
    },
    profile: mockProfile()
  }
}

/** random ids to be used in place of guids */
export const randomId = (prefix: string): string => {
  return `${prefix}${Math.floor(Math.random() * 1000)}`
}
