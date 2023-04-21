import React, {useState} from 'react'
import {SurveyModel} from "survey-core";
import {getEnvSpec} from "api/api";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faToggleOff, faToggleOn} from "@fortawesome/free-solid-svg-icons";

export default function SurveyReviewModeButton({surveyModel}: { surveyModel: SurveyModel | null }) {
  const [isReviewMode, setIsReviewMode] = useState(false)
  const {envName} = getEnvSpec()

  const toggleSurveyVisibilityMode = () => {
    if (surveyModel) {
      surveyModel.showInvisibleElements = !surveyModel.showInvisibleElements
      surveyModel.checkErrorsMode = surveyModel.showInvisibleElements ? "onComplete" : "onNextPage"
      setIsReviewMode(!isReviewMode)
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
