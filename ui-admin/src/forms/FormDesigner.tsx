import { flow, identity, set, update } from 'lodash/fp'
import React, { useState } from 'react'

import { FormContent, FormContentPage, FormPanel, HtmlElement, Question } from '@juniper/ui-core'

import { HtmlDesigner } from './designer/HtmlDesigner'
import { PageDesigner } from './designer/PageDesigner'
import { PanelDesigner } from './designer/PanelDesigner'
import { QuestionDesigner } from './designer/QuestionDesigner'
import { QuestionTemplatesDesigner } from './designer/QuestionTemplatesDesigner'
import { FormTableOfContents } from './FormTableOfContents'
import { PageListDesigner } from './designer/PageListDesigner'
import { useSearchParams } from 'react-router-dom'
import { Modal } from 'react-bootstrap'
import { NewQuestionForm } from './designer/NewQuestionForm'
import _cloneDeep from 'lodash/cloneDeep'
import { getContainerElementPath, getCurrentElementIndex, getSurveyElementFromPath } from './designer/designer-utils'

type FormDesignerProps = {
  readOnly?: boolean
  content: FormContent
  onChange: (editedContent: FormContent, callback?: () => void) => void
}

type SelectedElementType = 'pages' | 'questionTemplates' | 'page' | 'panel' | 'question' | 'none' | 'html'

/** UI for editing forms. */
export const FormDesigner = (props: FormDesignerProps) => {
  const { readOnly = false, content, onChange } = props
  const [showCreateQuestionModal, setShowCreateQuestionModal] = useState(false)
  const [searchParams, setSearchParams] = useSearchParams()
  const selectedElementPath = searchParams.get('selectedElementPath') ?? 'pages'
  const selectedElement = getSurveyElementFromPath(selectedElementPath, content)
  const setSelectedElementPath = (path: string) => {
    searchParams.set('selectedElementPath', path)
    setSearchParams(searchParams)
  }

  // selectedElementType is used to determine which designer to show and where to add a new question
  // it's based on selectedElement.type, but we also need to handle the case where selectedElement a template or root
  let selectedElementType: SelectedElementType = 'none'
  if (selectedElementPath === 'pages') {
    selectedElementType = 'pages'
  } else if (selectedElementPath === 'questionTemplates') {
    selectedElementType = 'questionTemplates'
  } else if (selectedElement) {
    if (!('type' in selectedElement) && !('questionTemplateName' in selectedElement)) {
      selectedElementType = 'page'
    } else if ('type' in selectedElement && selectedElement.type === 'panel') {
      selectedElementType = 'panel'
    } else if ('type' in selectedElement && selectedElement.type === 'html') {
      selectedElementType = 'html'
    } else {
      selectedElementType = 'question'
    }
  }


  const insertQuestionAtCursor = (newQuestion: Question) => {
    if (['pages', 'questionTemplates', 'none'].includes(selectedElementType)) {
      // we don't know what to add the question to
      return
    }
    const newValue = _cloneDeep(content)
    const containerElementPath = getContainerElementPath(selectedElementPath, newValue)
    const containerToUpdate = getSurveyElementFromPath(containerElementPath, newValue) as FormContentPage | FormPanel
    let newQuestionIndex = 0
    if (selectedElementType === 'question' || selectedElementType === 'html') {
      // add the new question after the selected question
      const questionIndex = getCurrentElementIndex(selectedElementPath)!
      newQuestionIndex = questionIndex + 1
      containerToUpdate.elements.splice(newQuestionIndex, 0, newQuestion)
    } else {
      // add the new question to the end of the page/panel
      newQuestionIndex = containerToUpdate.elements.length
      containerToUpdate.elements.push(newQuestion)
    }
    // we want to view the new question, but we need to wait for the state change to propagate before it will exist
    onChange(newValue, () => setSelectedElementPath(`${containerElementPath}.elements[${newQuestionIndex}]`))
  }

  const addQuestion = () => {
    setShowCreateQuestionModal(true)
  }

  return (
    <div className="overflow-hidden flex-grow-1 d-flex flex-row mh-100" style={{ flexBasis: 0 }}>
      <div className="flex-shrink-0 border-end" style={{ width: 400, overflowY: 'scroll' }}>
        <FormTableOfContents
          formContent={content}
          selectedElementPath={selectedElementPath}
          onSelectElement={setSelectedElementPath}
        />
      </div>
      <div className="flex-grow-1 overflow-scroll py-2 px-3">
        {(() => {
          if (selectedElementType === 'pages') {
            return (
              <PageListDesigner
                setSelectedElementPath={setSelectedElementPath}
                formContent={content}
                readOnly={readOnly}
                onChange={onChange}
              />
            )
          }

          if (selectedElementType === 'questionTemplates') {
            return (
              <QuestionTemplatesDesigner
                formContent={content}
                readOnly={readOnly}
                onChange={onChange}
              />
            )
          }
          if (selectedElementType === 'none') {
            return (
              <p className="mt-5 text-center">Select an element to edit</p>
            )
          }

          if (selectedElementType === 'page') {
            return (
              <PageDesigner
                readOnly={readOnly}
                value={selectedElement as FormContentPage}
                onChange={updatedElement => {
                  onChange(set(selectedElementPath, updatedElement, content))
                }}
                selectedElementPath={selectedElementPath}
                setSelectedElementPath={setSelectedElementPath}
                addNextQuestion={addQuestion}
              />
            )
          }

          if (selectedElementType === 'panel') {
            return (
              <PanelDesigner
                readOnly={readOnly}
                panel={selectedElement as FormPanel}
                selectedElementPath={selectedElementPath}
                setSelectedElementPath={setSelectedElementPath}
                addNextQuestion={addQuestion}
                onChange={(updatedElement, removedElement) => {
                  // The path to a panel will always end in an array index since the panel will be
                  // inside an elements array. Extract the path to that elements array and the
                  // selected element's index in it.
                  // eslint-disable-next-line max-len,@typescript-eslint/no-non-null-assertion
                  const [, parentElementsListPath, indexOfSelectedElementString] = selectedElementPath.match(/(.*?)\[(\d)+\]$/)!
                  const indexOfSelectedElement = parseInt(indexOfSelectedElementString)

                  onChange(
                    flow(
                      // Update the panel.
                      set(selectedElementPath, updatedElement),
                      // If an element was removed from the panel, add the removed element into the panel's
                      // parent's elements array after the panel itself.
                      removedElement
                        ? update(
                          parentElementsListPath,
                          elements => {
                            return [
                              ...elements.slice(0, indexOfSelectedElement + 1),
                              removedElement,
                              ...elements.slice(indexOfSelectedElement + 1)
                            ]
                          }
                        )
                        : identity
                    )(content)
                  )
                }}
              />
            )
          }

          if (selectedElementType === 'html') {
            return (
              <HtmlDesigner
                element={selectedElement as HtmlElement}
                readOnly={readOnly}
                addNextQuestion={addQuestion}
                onChange={updatedElement => {
                  onChange(set(selectedElementPath, updatedElement, content))
                }}
              />
            )
          }

          return (
            <QuestionDesigner
              question={selectedElement as Question}
              isNewQuestion={false}
              readOnly={readOnly}
              showName={true}
              addNextQuestion={addQuestion}
              onChange={updatedElement => {
                onChange(set(selectedElementPath, updatedElement, content))
              }}
            />
          )
        })()}
      </div>
      {showCreateQuestionModal && (
        <Modal show className="modal-lg" onHide={() => setShowCreateQuestionModal(false)}>
          <Modal.Header closeButton>New Question</Modal.Header>
          <Modal.Body>
            <NewQuestionForm
              readOnly={readOnly}
              questionTemplates={content.questionTemplates || []}
              onCreate={newQuestion => {
                setShowCreateQuestionModal(false)
                insertQuestionAtCursor(newQuestion)
              }}
            />
          </Modal.Body>
        </Modal>
      )}
    </div>
  )
}

