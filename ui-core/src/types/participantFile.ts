import { Answer } from 'src/types/forms'

export type ParticipantFile = {
  id?: string
  fileName: string
  fileType: string
  createdAt: number
  lastUpdatedAt: number
  associatedAnswers: Answer[]
}
