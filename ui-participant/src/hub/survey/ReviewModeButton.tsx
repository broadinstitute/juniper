import React, { useState } from 'react'
import { SurveyModel } from 'survey-core'
import { getEnvSpec } from 'api/api'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faToggleOff, faToggleOn } from '@fortawesome/free-solid-svg-icons'

/** button for facilitating quick review of survey contents without validation or visibility constraints */
export default function SurveyReviewModeButton({ surveyModel }: { surveyModel: SurveyModel | null }) {
  const [isReviewMode, setIsReviewMode] = useState(!!surveyModel?.showInvisibleElements)
  const { envName } = getEnvSpec()

  const toggleSurveyVisibilityMode = () => {
    if (surveyModel) {
      const newMode = !isReviewMode
      surveyModel.showInvisibleElements = newMode
      surveyModel.checkErrorsMode = newMode ? 'onComplete' : 'onNextPage'
      setIsReviewMode(newMode)
    }
  }
  if (envName === 'live') {
    return null
  }
  return <button className="float-end btn" aria-label="toggle show all items"
    title="toggle showing all items, regardless of visibility rules.
                                   (Available only in sandbox & irb environments)"
    onClick={toggleSurveyVisibilityMode}>
    Review mode: <FontAwesomeIcon icon={isReviewMode ? faToggleOn : faToggleOff} title={isReviewMode ? 'on' : 'off'}/>
  </button>
}
