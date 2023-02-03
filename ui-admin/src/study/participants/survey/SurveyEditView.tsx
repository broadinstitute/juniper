import React from 'react'
import { Survey, SurveyResponse } from 'api/api'

/** allows editing of a survey response */
export default function SurveyEditView({ response, survey }: {response: SurveyResponse, survey: Survey}) {
  console.log(`not implemented ${response.surveyId} ${survey.stableId}`)
  return <div>
    Not yet implemented
  </div>
}
