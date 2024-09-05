import {
  AnswerMapping,
  FormContent
} from '@juniper/ui-core'

export type OnChangeFormContent = (...args:
                                     [validationErrors: string[], newValue: FormContent] |
                                     [validationErrors: string[], newValue: undefined]
) => void

export type OnChangeAnswerMappings = (validationErrors: string[], newValue: AnswerMapping[]) => void
