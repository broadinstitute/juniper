export const answerMappingTargetTypes = ['PROFILE', 'PROXY', 'PROXY_PROFILE'] as const
export type AnswerMappingTargetType = typeof answerMappingTargetTypes[number];

export const answerMappingMapTypes = ['STRING_TO_STRING', 'STRING_TO_LOCAL_DATE', 'STRING_TO_BOOLEAN'] as const
export type AnswerMappingMapType = typeof answerMappingMapTypes[number];

export type AnswerMapping = {
  id: string
  questionStableId: string
  surveyId: string
  targetType: AnswerMappingTargetType
  targetField: string
  mapType: AnswerMappingMapType
  formatString: string
  errorOnFail: boolean
}

export type VersionedForm = {
  id: string
  stableId: string
  version: number
  publishedVersion?: number
  name: string
  createdAt: number
  lastUpdatedAt: number
  content: string
  answerMappings?: AnswerMapping[]
  footer?: string
}

export type SurveyType = 'RESEARCH' | 'OUTREACH' | 'CONSENT' | 'ADMIN'

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
  creatingParticipantUserId?: string
  creatingAdminUserId?: string
  createdAt?: number
  lastUpdatedAt?: number
}

export type FormResponse = {
  id?: string
  creatingParticipantUserId?: string
  creatingAdminUserId?: string
  resumeData: string
  createdAt?: number
  lastUpdatedAt?: number
}

export type SurveyResponse = FormResponse & {
  surveyId: string
  enrolleeId: string
  answers: Answer[]
  complete: boolean
}

export type SurveyResponseWithJustification = SurveyResponse & {
  justification?: string
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
  calculatedValues?: CalculatedValue[]
}

export type CalculatedValue = {
  name: string,
  expression: string,
  includeIntoResult: boolean
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
 */
export type I18nMap = {
  [language: string]: string
}

export type I18nSurveyElement = string | I18nMap


export type FormElement = FormPanel | HtmlElement | Question

export type FormPanel = BaseElement & {
  title: string
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
  title?: I18nSurveyElement
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
