import React, { useState } from 'react'
import { Survey as SurveyJSComponent } from 'survey-react-ui'

import { FormContent, surveyJSModelFromFormContent } from '@juniper/ui-core'

type SurveyPreviewProps = {
  survey: FormContent
}

export const SurveyPreview = (props: SurveyPreviewProps) => {
  const { survey } = props

  const [surveyModel] = useState(() => surveyJSModelFromFormContent(survey))

  return (
    <SurveyJSComponent model={surveyModel} />
  )
}
