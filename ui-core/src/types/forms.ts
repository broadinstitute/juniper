import { IPage, Question } from 'survey-core'

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

// Survey configuration

/** these types are vague as we're still deciding how much custom stuff we need on top of SurveyJS */
export type JuniperSurvey = {
  pages: IPage[]
  questionTemplates: JuniperQuestion[]
}

/** things that we need from SurveyJS elements to render the sheet view */
export type ElementBase = {
  name: string
  type: string
  title: string
}

/** Encompasses SurveyJS pages and panels. */
export type ElementContainer = {
  name: string
  elements: ElementBase[]
}

/**
 * We're extending SurveyJS to support templates -- the idea that a common question format may recur many times
 * in a survey, and so should only be coded once
 */
export type JuniperQuestion = Question & {
  questionTemplateName?: string
  type: string
}

export {}
