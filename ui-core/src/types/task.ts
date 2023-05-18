export type ParticipantTask = {
  id: string
  completedAt?: number
  status: ParticipantTaskStatus
  taskType: string
  targetName: string
  targetStableId: string
  targetAssignedVersion: number
  taskOrder: number
  blocksHub: boolean
  createdAt: number
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
