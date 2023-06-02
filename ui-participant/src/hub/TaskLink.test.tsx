import { Enrollee } from 'api/api'

import { isTaskAccessible } from './TaskLink'

const enrollee: Enrollee = {
  id: '1ad49d55-637a-49b9-8265-9bceddf997de',
  createdAt: 1685109523.300554,
  lastUpdatedAt: 1685656914.51655,
  participantUserId: '6710e92e-ccac-47de-a6f0-8c88b99f56da',
  profileId: '0a5e1901-5ea9-40da-a059-fa32448d8995',
  profile: {
    sexAtBirth: ''
  },
  studyEnvironmentId: 'b9069653-609d-4826-be3f-dce07b9bccf9',
  preEnrollmentResponseId: '5611210f-70d4-4806-94ac-cc5165054543',
  shortcode: 'TEST',
  consented: true,
  surveyResponses: [],
  consentResponses: [],
  kitRequests: [],
  participantTasks: [
    {
      id: '3bd8e991-2b1b-4cc8-be8b-c088b4f0632d',
      createdAt: 1685109523.300554,
      lastUpdatedAt: 1685656914.46739,
      completedAt: 1685656914.467386,
      status: 'COMPLETE',
      taskType: 'CONSENT',
      targetName: 'OurHealth Consent',
      targetStableId: 'oh_oh_consent',
      targetAssignedVersion: 1,
      taskOrder: 0,
      blocksHub: true,
      studyEnvironmentId: 'b9069653-609d-4826-be3f-dce07b9bccf9',
      enrolleeId: '1ad49d55-637a-49b9-8265-9bceddf997de',
      portalParticipantUserId: 'e6011367-f59c-45fd-8565-e9fe10fcb1b3',
      consentResponseId: '001fab2e-105b-4f54-bf16-3d6a19239efa'
    },
    {
      id: '6ed3001d-d9f0-4355-bda3-6ea1e36941ba',
      createdAt: 1685109523.300554,
      lastUpdatedAt: 1685657098.391093,
      completedAt: 1685657098.391092,
      status: 'COMPLETE',
      taskType: 'SURVEY',
      targetName: 'The Basics',
      targetStableId: 'oh_oh_basicInfo',
      targetAssignedVersion: 1,
      taskOrder: 0,
      blocksHub: true,
      studyEnvironmentId: 'b9069653-609d-4826-be3f-dce07b9bccf9',
      enrolleeId: '1ad49d55-637a-49b9-8265-9bceddf997de',
      portalParticipantUserId: 'e6011367-f59c-45fd-8565-e9fe10fcb1b3',
      surveyResponseId: 'c43a632d-2a57-418f-8337-c3281277d1dd'
    },
    {
      id: 'c4479f72-852d-42f8-9188-2330f2a3bfe6',
      createdAt: 1685109523.300554,
      lastUpdatedAt: 1685656723.280386,
      status: 'NEW',
      taskType: 'SURVEY',
      targetName: 'Cardiometabolic Medical History',
      targetStableId: 'oh_oh_cardioHx',
      targetAssignedVersion: 1,
      taskOrder: 1,
      blocksHub: true,
      studyEnvironmentId: 'b9069653-609d-4826-be3f-dce07b9bccf9',
      enrolleeId: '1ad49d55-637a-49b9-8265-9bceddf997de',
      portalParticipantUserId: 'e6011367-f59c-45fd-8565-e9fe10fcb1b3'
    },
    {
      id: 'e6a57854-a9bb-4489-a2db-b205826d7500',
      createdAt: 1685109523.300554,
      lastUpdatedAt: 1685656723.280393,
      status: 'NEW',
      taskType: 'SURVEY',
      targetName: 'Family History',
      targetStableId: 'oh_oh_famHx',
      targetAssignedVersion: 1,
      taskOrder: 3,
      blocksHub: false,
      studyEnvironmentId: 'b9069653-609d-4826-be3f-dce07b9bccf9',
      enrolleeId: '1ad49d55-637a-49b9-8265-9bceddf997de',
      portalParticipantUserId: 'e6011367-f59c-45fd-8565-e9fe10fcb1b3'
    }
  ]
}

describe('isTaskAccessible', () => {
  it('always returns true for completed tasks', () => {
    expect(isTaskAccessible(enrollee.participantTasks[1], enrollee)).toBe(true)
  })
})
