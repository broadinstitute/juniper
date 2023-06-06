import React, { useState } from 'react'
import { SurveyModel } from 'survey-core'
import { Survey as SurveyJSComponent } from 'survey-react-ui'

import { extractSurveyContent, FormContent, Survey } from '@juniper/ui-core'

type SurveyPreviewProps = {
  survey: FormContent
}

export const SurveyPreview = (props: SurveyPreviewProps) => {
  const { survey } = props

  const [surveyModel] = useState(() => {
    const fakeForm = { content: JSON.stringify(survey) } as Survey

    const model = new SurveyModel(extractSurveyContent(fakeForm))
    model.focusFirstQuestionAutomatic = false
    model.showTitle = false
    model.widthMode = 'static'

    return model
  })

  return (
    <SurveyJSComponent model={surveyModel} />
  )
}
