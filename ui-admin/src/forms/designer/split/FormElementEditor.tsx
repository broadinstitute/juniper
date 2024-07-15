import { FormContent, FormElement, FormPanel, PortalEnvironmentLanguage, Question } from '@juniper/ui-core'
import { TextInput } from 'components/forms/TextInput'
import { IconButton } from 'components/forms/Button'
import { faLock, faUnlock } from '@fortawesome/free-solid-svg-icons'
import QuestionTypeSelector from './QuestionTypeSelector'
import { QuestionDesigner } from '../QuestionDesigner'
import { PanelEditor } from './PanelEditor'
import React, { useState } from 'react'

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
  const [isStableIdLocked, setIsStableIdLocked] = useState(true)
  // @ts-ignore
  const elementType = element.type === 'panel' ? 'panel' : 'question'

  return (
    <>
      {elementType === 'question' ? (
        <>
          <label className={'form-label fw-semibold'}>Stable ID</label>
          <div className="d-flex align-items-center">
            <div className="w-100">
              <TextInput
                className={'mb-2'}
                value={(element as Question).name}
                disabled={isStableIdLocked}
                onChange={name => {
                  const newContent = { ...editedContent }
                  newContent.pages[currentPageNo].elements[elementIndex] = {
                    ...newContent.pages[currentPageNo].elements[elementIndex],
                    name
                  } as Question
                  onChange(newContent)
                }}
              />
            </div>
            <IconButton
              className="mb-2"
              icon={isStableIdLocked ? faLock : faUnlock}
              aria-label={isStableIdLocked ? 'Unlock stable ID' : 'Lock stable ID'}
              onClick={() => setIsStableIdLocked(!isStableIdLocked)}
            />
          </div>
          <QuestionTypeSelector
            // @ts-ignore
            key={element.name}
            // @ts-ignore
            questionType={(element as Question).type}
            onChange={newType => {
              const newContent = { ...editedContent }
              newContent.pages[currentPageNo].elements[elementIndex] = {
                ...newContent.pages[currentPageNo].elements[elementIndex],
                type: newType
              } as Question
              onChange(newContent)
            }}
          />
          <QuestionDesigner
            question={element as Question}
            isNewQuestion={false}
            showName={false}
            showQuestionTypeHeader={false}
            readOnly={false}
            onChange={newQuestion => {
              const newContent = { ...editedContent }
              newContent.pages[currentPageNo].elements[elementIndex] = newQuestion
              onChange(newContent)
            }}
            currentLanguage={currentLanguage}
            supportedLanguages={supportedLanguages}
          />
        </>
      ) : (
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
      )}
    </>
  )
}
