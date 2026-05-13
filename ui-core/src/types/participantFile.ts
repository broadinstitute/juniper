import { Answer } from 'src/types/forms'

export type VirusScanResult = 'UNSCANNED' | 'CLEAN' | 'QUARANTINED'

export type DownloadRecord = {
  createdAt: number
  participantUserId?: string
  adminUserId?: string
  enrolleeId: string
}

export type ParticipantFile = {
  id?: string
  fileName: string
  fileType: string
  createdAt: number
  lastUpdatedAt: number
  externalFileId: string
  creatingParticipantUserId?: string
  creatingAdminUserId?: string
  associatedAnswers: Answer[]
  downloads: DownloadRecord[]
  virusScanResult: VirusScanResult
}

export type FileAnswer = {
  fileName: string
  uploadedAt?: number
  uploadingAdminUserId?: string
  uploadingParticipantUserId?: string
  downloads?: DownloadRecord[]
}
