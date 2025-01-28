import React from 'react'
import { ModalProps } from 'react-bootstrap'
import ThemedModal from 'components/ThemedModal'
import { SurveyQuestionDocumentRequest } from '@juniper/ui-core'

export class ThemedSurveyQuestionDocumentRequest extends SurveyQuestionDocumentRequest {
  override get baseModal(): React.ElementType<ModalProps> {
    return ThemedModal
  }
}
