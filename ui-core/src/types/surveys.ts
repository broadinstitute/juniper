export type VersionedForm = {
  id: string
  stableId: string
  version: number
  name: string
  createdAt: number
  lastUpdatedAt: number
  content: string
  footer?: string
}

export type Survey = VersionedForm

export type ConsentForm = VersionedForm

export type Answer = {
  questionStableId: string

  stringValue?: string
  numberValue?: number
  booleanValue?: boolean
  objectValue?: string

  otherDescription?: string
}

export type FormResponse = {
  id?: string
  creatingParticipantUserId?: string
  resumeData: string
  createdAt?: number
  lastUpdatedAt?: number
}

export type ConsentResponse = FormResponse & {
  consentFormId: string
  enrolleeId: string
  fullData: string
  completed: boolean
  consented: boolean
}

export type SurveyResponse = FormResponse & {
  surveyId: string
  enrolleeId: string
  answers: Answer[]
  complete: boolean
}

export type PreregistrationResponse = FormResponse & {
  surveyId: string
  answers: Answer[]
  fullData: string
  qualified: boolean
}

export type PreEnrollmentResponse = FormResponse & {
  surveyId: string
  studyEnvironmentId: string
  answers: Answer[]
  fullData: string
  qualified: boolean
}

export {}
