import { Question, QuestionType } from '@juniper/ui-core'

export const questionTypeLabels: Record<QuestionType, string> = {
  text: 'Text',
  checkbox: 'Checkbox',
  dropdown: 'Dropdown',
  radiogroup: 'Radio group',
  signaturepad: 'Signature',
  medications: 'Medications'
}

export const questionTypeDescriptions: Record<QuestionType, string> = {
  text: 'Prompts the participant to enter a text response.',
  checkbox: 'Shows choices as checkboxes and prompts the participant to select one or more responses.',
  dropdown: 'Prompts the participant to choose a response from a menu of choices.',
  radiogroup: 'Shows choices as radio buttons and prompts the participant to select a response.',
  signaturepad: 'Prompts the participant to sign a form.',
  medications: 'Prompts the participant to choose medications from a list.'
}

//Returns an object with all possible fields for each question type
export const baseQuestions: Record<QuestionType, Question> = {
  checkbox: {
    type: 'checkbox',
    name: '',
    title: '',
    description: '',
    isRequired: false,
    choices: [],
    showNoneItem: undefined,
    noneText: undefined,
    noneValue: undefined,
    showOtherItem: undefined,
    otherText: undefined,
    otherPlaceholder: undefined,
    otherErrorText: undefined
  },
  dropdown: {
    type: 'dropdown',
    name: '',
    title: '',
    description: '',
    isRequired: false,
    choices: [],
    showOtherItem: undefined,
    otherText: undefined,
    otherPlaceholder: undefined,
    otherErrorText: undefined
  },
  medications: {
    type: 'medications',
    name: '',
    title: '',
    description: '',
    isRequired: false
  },
  radiogroup: {
    type: 'radiogroup',
    name: '',
    title: '',
    description: '',
    isRequired: false,
    choices: [],
    showOtherItem: undefined,
    otherText: undefined,
    otherPlaceholder: undefined,
    otherErrorText: undefined
  },
  signaturepad: {
    type: 'signaturepad',
    name: '',
    title: '',
    description: '',
    isRequired: false
  },
  text: {
    type: 'text',
    name: '',
    title: '',
    description: '',
    isRequired: false,
    inputType: 'text',
    min: undefined,
    max: undefined,
    size: undefined
  }
}
