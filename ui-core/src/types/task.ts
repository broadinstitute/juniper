export type ParticipantTask = {
  id: string
  blocksHub: boolean
  completedAt?: number
  createdAt: number
  enrolleeId: string
  lastUpdatedAt?: number
  portalParticipantUserId: string
  status: ParticipantTaskStatus
  studyEnvironmentId: string
  taskType: string
  targetName: string
  targetStableId: string
  targetAssignedVersion: number
  taskOrder: number

  // Tasks have one of these fields, depending on the task type.
  // TODO: Make this type a union of subtypes for each task type.
  consentResponseId?: string
  surveyResponseId?: string
}

export type ParticipantTaskStatus =
  | 'NEW'
  | 'IN_PROGRESS'
  | 'COMPLETE'
  | 'REJECTED'

export type ParticipantTaskType =
  | 'CONSENT'
  | 'SURVEY'
  | 'KIT_REQUEST'

export {}
