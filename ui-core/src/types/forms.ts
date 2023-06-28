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
  surveyStableId?: string
  surveyVersion?: number
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
  title: string
  description?: string
  isRequired?: boolean
}

type QuestionChoice = {
  text: string
  value: string
}

type WithOtherOption<T> = T & {
  showOtherItem?: boolean
  otherText?: string
  otherPlaceholder?: string
  otherErrorText?: string
}

export type CheckboxQuestion = WithOtherOption<BaseQuestion & {
  type: 'checkbox'
  choices: QuestionChoice[]
  showNoneItem?: boolean
  noneText?: string
  noneValue?: string
}>

export type DropdownQuestion = WithOtherOption<BaseQuestion & {
  type: 'dropdown'
  choices: QuestionChoice[]
}>

export type RadiogroupQuestion = WithOtherOption<BaseQuestion & {
  type: 'radiogroup'
  choices: QuestionChoice[]
}>

export type TemplatedQuestion = Omit<BaseQuestion, 'title'> & {
  name: string
  title?: string
  questionTemplateName: string
}

export type TextQuestion = BaseQuestion & {
  type: 'text'
  inputType?: 'text' | 'number'
  size?: number
  min?: number
  max?: number
}

export type SignatureQuestion = BaseQuestion & {
  type: 'signaturepad'
}

export type MedicationsQuestion = BaseQuestion & {
  type: 'medications'
}

export type Question =
  | CheckboxQuestion
  | DropdownQuestion
  | MedicationsQuestion
  | RadiogroupQuestion
  | SignatureQuestion
  | TemplatedQuestion
  | TextQuestion

/** Possible values for the 'type' field of a Question. */
export type QuestionType = Exclude<Question, TemplatedQuestion>['type']

export {}
