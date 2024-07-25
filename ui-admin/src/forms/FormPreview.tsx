import React, { useState } from 'react'
import { Survey as SurveyJSComponent } from 'survey-react-ui'
import 'survey-core/survey.i18n'

import {
  applyMarkdown,
  createAddressValidator,
  FormContent,
  PortalEnvironmentLanguage,
  surveyJSModelFromFormContent,
  useForceUpdate,
  useI18n
} from '@juniper/ui-core'

import { FormPreviewOptions } from './FormPreviewOptions'
import Api from 'api/api'
import useUpdateEffect from '../util/useUpdateEffect'

type FormPreviewProps = {
  formContent: FormContent
  currentLanguage: PortalEnvironmentLanguage
}

/**
 * Renders a preview of a form/survey.
 */
export const FormPreview = (props: FormPreviewProps) => {
  const { formContent, currentLanguage } = props

  const { i18n } = useI18n()

  const [surveyModel] = useState(() => {
    // note that this roughly mimics surveyUtils.newSurveyJSModel but with key differences, such
    // as the pages not being url-routable
    const model = surveyJSModelFromFormContent(formContent)
    model.setVariable('portalEnvironmentName', 'sandbox')
    model.setVariable('profile', { })
    model.setVariable('proxyProfile', { })
    model.setVariable('isGovernedUser', false)
    model.ignoreValidation = true
    model.locale = currentLanguage.languageCode
    model.onTextMarkdown.add(applyMarkdown)
    model.onServerValidateQuestions.add(createAddressValidator(addr => Api.validateAddress(addr), i18n))
    return model
  })
  const forceUpdate = useForceUpdate()
  useUpdateEffect(() => {
    surveyModel.locale = currentLanguage.languageCode
  }, [currentLanguage.languageCode])


  return (
    <div className="overflow-hidden flex-grow-1 d-flex flex-row mh-100" style={{ flexBasis: 0 }}>
      <div className="flex-grow-1 overflow-scroll">
        <SurveyJSComponent model={surveyModel} />
      </div>
      <div className="flex-shrink-0 p-3" style={{ width: 300 }}>
        <FormPreviewOptions
          value={{
            ignoreValidation: surveyModel.ignoreValidation,
            showInvisibleElements: surveyModel.showInvisibleElements,
            locale: surveyModel.locale,
            profile: surveyModel.getVariable('profile'),
            proxyProfile: surveyModel.getVariable('proxyProfile'),
            isGovernedUser: surveyModel.getVariable('isGovernedUser')
          }}
          onChange={({ ignoreValidation, showInvisibleElements, profile, proxyProfile, isGovernedUser }) => {
            surveyModel.ignoreValidation = ignoreValidation
            surveyModel.showInvisibleElements = showInvisibleElements
            surveyModel.setVariable('profile', profile)
            surveyModel.setVariable('proxyProfile', proxyProfile)
            surveyModel.setVariable('isGovernedUser', isGovernedUser)
            forceUpdate()
          }}
        />
      </div>
    </div>
  )
}
