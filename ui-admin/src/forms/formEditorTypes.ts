import { FormContent } from '@juniper/ui-core'

export type OnChangeFormContent = (...args:
                                     [validationError: string | undefined, newValue: FormContent] |
                                     [validationError: string, newValue: undefined] |
                                     [undefined, undefined]
) => void
