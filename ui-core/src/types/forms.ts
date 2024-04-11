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
  rule?: string
  assignToAllNewEnrollees: boolean
  assignToExistingEnrollees: boolean
  autoUpdateTaskAssignments: boolean
  recur: boolean
  recurrenceIntervalDays: number
  allowAdminEdit: boolean
  allowParticipantStart: boolean
  allowParticipantReedit: boolean
  prepopulate: boolean
  eligibilityRule?: string
}

export const defaultSurvey = {
  required: false,
  assignToAllNewEnrollees: true,
  assignToExistingEnrollees: false,
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
  viewedLanguage?: string
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
  title: I18nSurveyElement
  pages: FormContentPage[]
  questionTemplates?: Question[]
}

type BaseElement = {
  visibleIf?: string
}

export type FormContentPage = BaseElement & {
  elements: FormElement[]
}

/**
 *  Certain SurveyJS elements can take on multiple forms. For example, the "title" field
 *  for a question could either be a string, or an object mapping language codes to strings.
 *  "default" is always present; the other languages are arbitrary.
 */
export type I18nMap = {
  default: string,
  [language: string]: string
}

export type I18nSurveyElement = string | I18nMap


export type FormElement = FormPanel | HtmlElement | Question

export type FormPanel = BaseElement & {
  type: 'panel'
  elements: (HtmlElement | Question)[]
}

export type HtmlElement = {
  name: string
  type: 'html'
  html: I18nSurveyElement
}

type BaseQuestion = BaseElement & {
  name: string
  description?: I18nSurveyElement
  isRequired?: boolean
}

export type TitledQuestion = BaseQuestion & {
  title: I18nSurveyElement
}

export type QuestionChoice = {
  text: I18nSurveyElement
  value: string
}

type WithOtherOption<T> = T & {
  showOtherItem?: boolean
  otherText?: I18nSurveyElement
  otherPlaceholder?: I18nSurveyElement
  otherErrorText?: I18nSurveyElement
}

export type CheckboxQuestion = WithOtherOption<TitledQuestion & {
  type: 'checkbox'
  choices: QuestionChoice[]
  showNoneItem?: boolean
  noneText?: I18nSurveyElement
  noneValue?: I18nSurveyElement
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
  html: I18nSurveyElement
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
