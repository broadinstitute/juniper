import { QuestionType } from '@juniper/ui-core'

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
