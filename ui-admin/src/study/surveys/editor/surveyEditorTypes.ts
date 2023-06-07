import { FormContent } from '@juniper/ui-core'

export type OnChangeSurvey = (...args: [valid: true, newValue: FormContent] | [false, undefined]) => void
