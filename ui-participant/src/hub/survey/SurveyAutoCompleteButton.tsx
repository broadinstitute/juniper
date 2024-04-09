import React from 'react'
import { SurveyModel } from 'survey-core'
import { getEnvSpec } from 'api/api'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faWandSparkles } from '@fortawesome/free-solid-svg-icons'

/** button to automatically fill in a survey. this doesn't handle every single question type, but works with the more
 * commonly tested forms like preEnroll. Only available in sandbox and irb environments. */
export default function SurveyAutoCompleteButton({ surveyModel }: { surveyModel: SurveyModel | null }) {
  const { envName } = getEnvSpec()

  const autoCompleteSurvey = () => {
    if (surveyModel) {
      surveyModel.pages.forEach(page => {
        page.questions.forEach(question => {
          if (question.getType() === 'radiogroup' || 'checkbox' && question.choices && question.choices.length > 0) {
            surveyModel.setValue(question.name, question.choices[0].value)
          }
          if (question.getType() === 'text') {
            surveyModel.setValue(question.name, Math.random().toString(36).substring(5))
          }
        })
      })
      surveyModel.currentPageNo = surveyModel.pageCount - 1
    }
  }
  if (envName === 'live') {
    return null
  }
  return <button className="float-end btn" aria-label="automatically complete the survey"
    title="automatically complete the current survey. (Available only in sandbox & irb environments)"
    onClick={autoCompleteSurvey}>
    Auto-complete: <FontAwesomeIcon icon={faWandSparkles}/>
  </button>
}
