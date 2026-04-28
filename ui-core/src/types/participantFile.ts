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
  associatedAnswers: Answer[]
  downloads: DownloadRecord[]

  virusScanResult: VirusScanResult
}
