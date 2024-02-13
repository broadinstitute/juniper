import { PortalEnvironmentLanguage } from './portal'

export type VersionedForm = {
  id: string
  stableId: string
  version: number
  publishedVersion?: number
  name: string
  createdAt: number
  lastUpdatedAt: number
  content: string
  footer?: string
}

export type SurveyType = 'RESEARCH' | 'OUTREACH'

export type Survey = VersionedForm & {
  surveyType: SurveyType
  blurb?: string
  required: boolean
  assignToAllNewEnrollees: boolean
  autoUpdateTaskAssignments: boolean
  recur: boolean
  recurrenceIntervalDays: number
  allowAdminEdit: boolean
  allowParticipantStart: boolean
  allowParticipantReedit: boolean
  prepopulate: boolean
}

export const defaultSurvey = {
  required: false,
  assignToAllNewEnrollees: true,
  autoUpdateTaskAssignments: false,
  recur: false,
  recurrenceIntervalDays: 0,
  allowAdminEdit: true,
  allowParticipantStart: true,
  allowParticipantReedit: true,
  prepopulate: false
}

export type ConsentForm = VersionedForm

export type Answer = {
  questionStableId: string
  stringValue?: string
  numberValue?: number
  booleanValue?: boolean
  objectValue?: string
  otherDescription?: string
  surveyStableId?: string
  surveyVersion?: number
  viewedLanguage?: PortalEnvironmentLanguage
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

/** Configuration passed to SurveyModel constructor. */
export type FormContent = {
  title: string
  pages: FormContentPage[]
  questionTemplates?: Question[]
}

type BaseElement = {
  visibleIf?: string
}

export type FormContentPage = BaseElement & {
  elements: FormElement[]
}

export type FormElement = FormPanel | HtmlElement | Question

export type FormPanel = BaseElement & {
  type: 'panel'
  elements: (HtmlElement | Question)[]
}

export type HtmlElement = {
  name: string
  type: 'html'
  html: string
}

type BaseQuestion = BaseElement & {
  name: string
  description?: string
  isRequired?: boolean
}

export type TitledQuestion = BaseQuestion & {
    title: string
}

export type QuestionChoice = {
  text: string
  value: string
}

type WithOtherOption<T> = T & {
  showOtherItem?: boolean
  otherText?: string
  otherPlaceholder?: string
  otherErrorText?: string
}

export type CheckboxQuestion = WithOtherOption<TitledQuestion & {
  type: 'checkbox'
  choices: QuestionChoice[]
  showNoneItem?: boolean
  noneText?: string
  noneValue?: string
}>

export type DropdownQuestion = WithOtherOption<TitledQuestion & {
  type: 'dropdown'
  choices: QuestionChoice[]
}>

export type RadiogroupQuestion = WithOtherOption<TitledQuestion & {
  type: 'radiogroup'
  choices: QuestionChoice[]
}>

export type TemplatedQuestion = BaseQuestion & {
  name: string
  title?: string
  questionTemplateName: string
}

export type TextQuestion = TitledQuestion & {
  type: 'text'
  inputType?: 'text' | 'number'
  size?: number
  min?: number
  max?: number
}

export type SignatureQuestion = TitledQuestion & {
  type: 'signaturepad'
}

export type MedicationsQuestion = TitledQuestion & {
  type: 'medications'
}

export type HtmlQuestion = BaseQuestion & {
  type: 'html',
  html: string
}

export type Question =
  | CheckboxQuestion
  | DropdownQuestion
  | MedicationsQuestion
  | RadiogroupQuestion
  | SignatureQuestion
  | TemplatedQuestion
  | TextQuestion
  | HtmlQuestion

export type InteractiveQuestion = Exclude<Question, HtmlQuestion>

/** Possible values for the 'type' field of a Question. */
export type QuestionType = Exclude<Question, TemplatedQuestion>['type']

export {}
