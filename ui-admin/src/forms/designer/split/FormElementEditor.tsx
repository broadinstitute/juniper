import { FormContent, FormElement, FormPanel, PortalEnvironmentLanguage, Question } from '@juniper/ui-core'
import { PanelEditor } from './PanelEditor'
import React from 'react'
import { FullQuestionDesigner } from './FullQuestionDesigner'

/**
 * An editor for a form element (question or panel).
 */
export const FormElementEditor = ({
  element, elementIndex, currentPageNo, editedContent, onChange, currentLanguage,
  supportedLanguages
}: {
  element: FormElement, elementIndex: number, currentPageNo: number,
  currentLanguage: PortalEnvironmentLanguage, supportedLanguages: PortalEnvironmentLanguage[],
  editedContent: FormContent, onChange: (newContent: FormContent) => void
}) => {
  let elementType: string
  if (!('type' in element) && !('questionTemplateName' in element)) {
    elementType = 'page'
  } else if ('type' in element && element.type === 'panel') {
    elementType = 'panel'
  } else if ('type' in element && element.type === 'html') {
    elementType = 'html'
  } else {
    elementType = 'question'
  }

  return (
    <>
      {elementType === 'question' &&
        <FullQuestionDesigner
          question={element as Question}
          isNewQuestion={false}
          readOnly={false}
          onChange={newQuestion => {
            const newContent = { ...editedContent }
            newContent.pages[currentPageNo].elements[elementIndex] = newQuestion
            onChange(newContent)
          }}
          currentLanguage={currentLanguage}
          supportedLanguages={supportedLanguages}
        />
      }
      {elementType === 'panel' &&
        <PanelEditor
          panel={element as FormPanel}
          onChange={newPanel => {
            const newContent = { ...editedContent }
            newContent.pages[currentPageNo].elements[elementIndex] = newPanel
            onChange(newContent)
          }}
          currentLanguage={currentLanguage}
          supportedLanguages={supportedLanguages}
        />
      }
    </>
  )
}
