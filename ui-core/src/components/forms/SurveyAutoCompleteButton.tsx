import React from 'react'
import { SurveyModel } from 'survey-core'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faWandSparkles } from '@fortawesome/free-solid-svg-icons'
import { EnvironmentName } from 'src/types/study'
import { isEmpty, isNumber } from 'lodash'

/** button to automatically fill in a survey. this doesn't handle every single question type, but works with the more
 * commonly tested forms like preEnroll. Only available in sandbox and irb environments. */
export function SurveyAutoCompleteButton({ surveyModel, envName }: {
    surveyModel: SurveyModel | null, envName: EnvironmentName
}) {
  const autoCompleteSurvey = () => {
    if (surveyModel) {
      surveyModel.pages.forEach(page => {
        page.questions.forEach(question => {
          const value = surveyModel.getValue(question.name)
          if (question.readOnly
            || question.isReadOnly
            || (!isEmpty(value) || (isNumber(value) && value > 0))
          ) {
            return
          }

          if (question.getType() === 'radiogroup' || 'checkbox' && question.choices && question.choices.length > 0) {
            surveyModel.setValue(question.name, question.choices[0].value)
          }
          if (question.getType() === 'text') {
            surveyModel.setValue(question.name, Math.random().toString(36).substring(5))
          }
          if (question.getType() === 'signaturepad') {
            surveyModel.setValue(question.name, 'data:image/png;base64,')
          }
        })
      })
      surveyModel.currentPageNo = surveyModel.pageCount - 1
    }
  }
  if (envName === 'live') {
    return null
  }
  return <button className="float-end btn" aria-label="automatically fill in the survey"
    title="Automatically fill in the current survey. (Available only in sandbox & irb environments)"
    onClick={autoCompleteSurvey}>
    Autofill: <FontAwesomeIcon icon={faWandSparkles}/>
  </button>
}
