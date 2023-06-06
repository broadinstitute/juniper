import { JuniperSurvey } from '@juniper/ui-core'

export type OnChangeSurvey = (...args: [valid: true, newValue: JuniperSurvey] | [false, undefined]) => void
