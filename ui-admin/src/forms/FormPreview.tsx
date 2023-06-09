import React, { useState } from 'react'
import { Survey as SurveyJSComponent } from 'survey-react-ui'

import { FormContent, surveyJSModelFromFormContent } from '@juniper/ui-core'

type FormPreviewProps = {
  formContent: FormContent
}

export const FormPreview = (props: FormPreviewProps) => {
  const { formContent } = props

  const [surveyModel] = useState(() => {
    const model = surveyJSModelFromFormContent(formContent)
    model.setVariable('portalEnvironmentName', 'sandbox')
    return model
  })

  return (
    <SurveyJSComponent model={surveyModel} />
  )
}
