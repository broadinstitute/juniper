import React, { useState } from 'react'
import { Survey as SurveyJSComponent } from 'survey-react-ui'

import { FormContent, PortalLanguage, surveyJSModelFromFormContent, useForceUpdate } from '@juniper/ui-core'

import { FormPreviewOptions } from './FormPreviewOptions'

type FormPreviewProps = {
  formContent: FormContent
  supportedLanguages: PortalLanguage[]
}

// TODO: Add JSDoc
// eslint-disable-next-line jsdoc/require-jsdoc
export const FormPreview = (props: FormPreviewProps) => {
  const { formContent, supportedLanguages } = props

  const [surveyModel] = useState(() => {
    const model = surveyJSModelFromFormContent(formContent)
    model.setVariable('portalEnvironmentName', 'sandbox')
    model.ignoreValidation = true
    model.locale = 'default'
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
          supportedLanguages={supportedLanguages}
          value={{
            ignoreValidation: surveyModel.ignoreValidation,
            showInvisibleElements: surveyModel.showInvisibleElements,
            locale: surveyModel.locale
          }}
          onChange={({ ignoreValidation, showInvisibleElements, locale }) => {
            surveyModel.ignoreValidation = ignoreValidation
            surveyModel.showInvisibleElements = showInvisibleElements
            surveyModel.locale = locale
            forceUpdate()
          }}
        />
      </div>
    </div>
  )
}
