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
  targetName?: string
  targetStableId?: string
  targetAssignedVersion?: number
  taskOrder: number
  participantNoteId?: string
  creatingAdminUserId?: string
  assignedAdminUserId?: string

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
  | 'VIEWED'
  | 'REMOVED'

export const ParticipantTaskStatusOptions: { label: string, value: ParticipantTaskStatus }[] = [
  { label: 'New', value: 'NEW' },
  { label: 'Viewed', value: 'VIEWED' },
  { label: 'In progress', value: 'IN_PROGRESS' },
  { label: 'Complete', value: 'COMPLETE' },
  { label: 'Rejected', value: 'REJECTED' }
]

export type ParticipantTaskType =
  | 'CONSENT'
  | 'SURVEY'
  | 'OUTREACH'
  | 'KIT_REQUEST'
  | 'ADMIN_FORM'
  | 'ADMIN_NOTE'

export {}
