import { FormContent } from '@juniper/ui-core'

export type OnChangeFormContent = (...args: [valid: true, newValue: FormContent] | [false, undefined]) => void
