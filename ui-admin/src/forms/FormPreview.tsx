import React, { useState } from 'react'
import { Survey as SurveyJSComponent } from 'survey-react-ui'

import { FormContent, surveyJSModelFromFormContent, useForceUpdate } from '@juniper/ui-core'

import { FormPreviewOptions } from './FormPreviewOptions'

type FormPreviewProps = {
  formContent: FormContent
}

export const FormPreview = (props: FormPreviewProps) => {
  const { formContent } = props

  const [surveyModel] = useState(() => {
    const model = surveyJSModelFromFormContent(formContent)
    model.setVariable('portalEnvironmentName', 'sandbox')
    model.ignoreValidation = true
    return model
  })
  const forceUpdate = useForceUpdate()

  return (
    <div className="overflow-hidden flex-grow-1 d-flex flex-row mh-100" style={{ flexBasis: 0 }}>
      <div className="flex-grow-1 overflow-scroll">
        <SurveyJSComponent model={surveyModel} />
      </div>
      <div className="flex-shrink-0 p-3" style={{ width: 300 }}>
        <FormPreviewOptions
          value={{
            ignoreValidation: surveyModel.ignoreValidation
          }}
          onChange={({ ignoreValidation }) => {
            surveyModel.ignoreValidation = ignoreValidation
            forceUpdate()
          }}
        />
      </div>
    </div>
  )
}
