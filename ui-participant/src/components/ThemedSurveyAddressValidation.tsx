import React from 'react'
import { ReactQuestionFactory } from 'survey-react-ui'
import { SurveyQuestionAddressValidation } from '@juniper/ui-core/build/surveyjs/address-validation-modal-question'
import ThemedModal from './ThemedModal'
import { ModalProps } from 'react-bootstrap'

export class ThemedSurveyQuestionAddressValidation extends SurveyQuestionAddressValidation {
  override get baseModal(): React.ElementType<ModalProps> {
    return ThemedModal
  }
}

// register themed address validation type
ReactQuestionFactory.Instance.registerQuestion('addressvalidation', props => {
  return React.createElement(ThemedSurveyQuestionAddressValidation, props)
})
