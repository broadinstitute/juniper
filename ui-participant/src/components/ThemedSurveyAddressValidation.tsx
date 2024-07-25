import React from 'react'
import { SurveyQuestionAddressValidation } from '@juniper/ui-core'
import ThemedModal from './ThemedModal'
import { ModalProps } from 'react-bootstrap'

export class ThemedSurveyQuestionAddressValidation extends SurveyQuestionAddressValidation {
  override get baseModal(): React.ElementType<ModalProps> {
    return ThemedModal
  }
}
