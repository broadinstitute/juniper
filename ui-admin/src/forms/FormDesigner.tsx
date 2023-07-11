import { flow, get, identity, set, update } from 'lodash/fp'
import React, { useState } from 'react'

import { FormContent, FormContentPage, FormElement } from '@juniper/ui-core'

import { HtmlDesigner } from './designer/HtmlDesigner'
import { PageDesigner } from './designer/PageDesigner'
import { PagesList } from './designer/PagesList'
import { PanelDesigner } from './designer/PanelDesigner'
import { QuestionDesigner } from './designer/QuestionDesigner'
import { QuestionTemplatesDesigner } from './designer/QuestionTemplatesDesigner'
import { FormTableOfContents } from './FormTableOfContents'
import {PageListDesigner} from "./designer/PageListDesigner";

type FormDesignerProps = {
  readOnly?: boolean
  value: FormContent
  onChange: (editedContent: FormContent) => void
}

/** UI for editing forms. */
export const FormDesigner = (props: FormDesignerProps) => {
  const { readOnly = false, value, onChange } = props

  const [selectedElementPath, setSelectedElementPath] = useState<string>()

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
          if (selectedElementPath === undefined) {
            return (
              <p className="mt-5 text-center">Select an element to edit</p>
            )
          }

          if (selectedElementPath === 'pages') {
            return (
              <PageListDesigner
                formContent={value}
                readOnly={readOnly}
                onChange={onChange}
              />
            )
          }

          if (selectedElementPath === 'questionTemplates') {
            return (
              <QuestionTemplatesDesigner
                formContent={value}
                readOnly={readOnly}
                onChange={onChange}
              />
            )
          }

          const selectedElement = get(selectedElementPath, value) as FormContentPage | FormElement

          if (!('type' in selectedElement) && !('questionTemplateName' in selectedElement)) {
            return (
              <PageDesigner
                readOnly={readOnly}
                value={selectedElement}
                onChange={updatedElement => {
                  onChange(set(selectedElementPath, updatedElement, value))
                }}
              />
            )
          }

          if ('type' in selectedElement && selectedElement.type === 'panel') {
            return (
              <PanelDesigner
                readOnly={readOnly}
                value={selectedElement}
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
                            console.log(elements)
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

          if ('type' in selectedElement && selectedElement.type === 'html') {
            return (
              <HtmlDesigner
                element={selectedElement}
                readOnly={readOnly}
                onChange={updatedElement => {
                  onChange(set(selectedElementPath, updatedElement, value))
                }}
              />
            )
          }

          return (
            <QuestionDesigner
              question={selectedElement}
              readOnly={readOnly}
              onChange={updatedElement => {
                onChange(set(selectedElementPath, updatedElement, value))
              }}
            />
          )
        })()}
      </div>
    </div>
  )
}
