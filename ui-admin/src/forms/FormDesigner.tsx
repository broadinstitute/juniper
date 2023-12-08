import { flow, get, identity, set, update } from 'lodash/fp'
import React, {useState} from 'react'

import {FormContent, FormContentPage, FormElement, FormPanel, HtmlElement, Question} from '@juniper/ui-core'

import { HtmlDesigner } from './designer/HtmlDesigner'
import { PageDesigner } from './designer/PageDesigner'
import { PanelDesigner } from './designer/PanelDesigner'
import { QuestionDesigner } from './designer/QuestionDesigner'
import { QuestionTemplatesDesigner } from './designer/QuestionTemplatesDesigner'
import { FormTableOfContents } from './FormTableOfContents'
import { PageListDesigner } from './designer/PageListDesigner'
import { useSearchParams } from 'react-router-dom'
import {Modal} from "react-bootstrap";
import {NewQuestionForm} from "./designer/NewQuestionForm";
import _cloneDeep from "lodash/cloneDeep";

type FormDesignerProps = {
  readOnly?: boolean
  value: FormContent
  onChange: (editedContent: FormContent) => void
}

type SelectedElementType = 'pages' | 'questionTemplates' | 'page' | 'panel' | 'question' | 'none' | 'html'

/** UI for editing forms. */
export const FormDesigner = (props: FormDesignerProps) => {
  const { readOnly = false, value, onChange } = props
  const [showCreateQuestionModal, setShowCreateQuestionModal] = useState(false)
  const [searchParams, setSearchParams] = useSearchParams()
  const selectedElementPath = searchParams.get('selectedElementPath') ?? 'pages'
  const selectedElement = getSurveyElementFromPath(selectedElementPath, value)
  const setSelectedElementPath = (path: string) => {
    searchParams.set('selectedElementPath', path)
    setSearchParams(searchParams)
  }

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


  const insertQuestion = (newQuestion: Question) => {
    if (['pages', 'questionTemplates', 'none'].includes(selectedElementType)) {
      // we don't know what to add the question to
      return
    }
    const newValue = _cloneDeep(value)
    let elementPathToUpdate = selectedElementPath
    if (selectedElementType === 'question') {

      elementPathToUpdate = selectedElementPath.substring(0, selectedElementPath.lastIndexOf('.elements['))
    }
    const elementToUpdate = getSurveyElementFromPath(elementPathToUpdate, newValue) as FormContentPage
    if (selectedElementType === 'question') {
      // add the new question after the selected question
      const questionIndex = parseInt(selectedElementPath.substring(selectedElementPath.lastIndexOf('.elements[') + 10,
          selectedElementPath.lastIndexOf(']')))
      elementToUpdate.elements.splice(questionIndex + 1, 0, newQuestion)
    } else {
      // add the new question to the end of the page/panel
      elementToUpdate.elements.push(newQuestion)
    }
    onChange(newValue)
  }

  return (
    <div className="overflow-hidden flex-grow-1 d-flex flex-row mh-100" style={{ flexBasis: 0 }}>
      <div className="flex-shrink-0 border-end" style={{ width: 400, overflowY: 'scroll' }}>
        <FormTableOfContents
          formContent={value}
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
                formContent={value}
                readOnly={readOnly}
                onChange={onChange}
              />
            )
          }

          if (selectedElementType === 'questionTemplates') {
            return (
              <QuestionTemplatesDesigner
                formContent={value}
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
                formContent={value}
                value={selectedElement as FormContentPage}
                onChange={updatedElement => {
                  onChange(set(selectedElementPath, updatedElement, value))
                }}
                selectedElementPath={selectedElementPath}
                setSelectedElementPath={setSelectedElementPath}
                setShowCreateQuestionModal={setShowCreateQuestionModal}
              />
            )
          }

          if (selectedElementType === 'panel') {
            return (
              <PanelDesigner
                readOnly={readOnly}
                value={selectedElement as FormPanel}
                selectedElementPath={selectedElementPath}
                setSelectedElementPath={setSelectedElementPath}
                setShowCreateQuestionModal={setShowCreateQuestionModal}
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
                    )(value)
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
                onChange={updatedElement => {
                  onChange(set(selectedElementPath, updatedElement, value))
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
              setShowCreateQuestionModal={setShowCreateQuestionModal}
              onChange={updatedElement => {
                onChange(set(selectedElementPath, updatedElement, value))
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
                  questionTemplates={value.questionTemplates || []}
                  onCreate={newQuestion => {
                    setShowCreateQuestionModal(false)
                    insertQuestion(newQuestion)
                  }}
              />
            </Modal.Body>
          </Modal>
      )}
    </div>
  )
}

const getSurveyElementFromPath = (elementPath: string, obj: object):  FormContentPage | FormElement | undefined => {
  try {
    return get(elementPath, obj) as FormContentPage | FormElement
  } catch (e) {
    return undefined
  }
}

