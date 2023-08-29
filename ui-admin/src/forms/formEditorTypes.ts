import { FormContent } from '@juniper/ui-core'

export type OnChangeFormContent = (...args:
                                     [validationErrors: string[], newValue: FormContent] |
                                     [validationErrors: string[], newValue: undefined]
) => void
