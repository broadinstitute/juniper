import React, { useState } from 'react'
import { Survey as SurveyJSComponent } from 'survey-react-ui'
import 'survey-core/survey.i18n'

import {
  createAddressValidator,
  FormContent,
  PortalEnvironmentLanguage,
  surveyJSModelFromFormContent,
  useForceUpdate,
  useI18n
} from '@juniper/ui-core'

import { FormPreviewOptions } from './FormPreviewOptions'
import Api from '../api/api'
import { usePortalLanguage } from '../portal/useDefaultPortalLanguage'

type FormPreviewProps = {
  formContent: FormContent
  supportedLanguages: PortalEnvironmentLanguage[]
}

/**
 * Renders a preview of a form/survey.
 */
export const FormPreview = (props: FormPreviewProps) => {
  const { formContent, supportedLanguages } = props
  const { defaultLanguage } = usePortalLanguage()

  const { i18n } = useI18n()

  const [surveyModel] = useState(() => {
    const model = surveyJSModelFromFormContent(formContent)
    model.setVariable('portalEnvironmentName', 'sandbox')
    model.ignoreValidation = true
    model.locale = defaultLanguage.languageCode
    model.onServerValidateQuestions.add(createAddressValidator(addr => Api.validateAddress(addr), i18n))
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
