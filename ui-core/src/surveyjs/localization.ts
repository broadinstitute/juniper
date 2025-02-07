import 'survey-core/survey.i18n'
import { surveyLocalization } from 'survey-core'

surveyLocalization.setupLocale(
  'zh',   // A short code used as a locale identifier (for example, "en", "de", "fr")
  surveyLocalization.getLocaleStrings('zh-cn'), // An array with custom translations,
  '中文',
  'Chinese')

