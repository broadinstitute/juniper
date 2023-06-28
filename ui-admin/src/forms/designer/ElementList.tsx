import { faChevronDown, faChevronUp, faTimes } from '@fortawesome/free-solid-svg-icons'
import React, { useState } from 'react'

import { FormElement, FormPanel } from '@juniper/ui-core'

import { IconButton } from 'components/forms/Button'

import { DeletePanelConfirmationModal } from './DeletePanelConfirmationModal'
import { getElementLabel } from './designer-utils'

type ElementListProps<T extends FormElement> = {
  readOnly: boolean
  value: T[]
  onChange: (newValue: T[]) => void
}

/** UI for re-ordering a list of form elements. */
export const ElementList = <T extends FormElement, >(props: ElementListProps<T>) => {
  const { readOnly, value, onChange } = props

  const [confirmingDeletePanel, setConfirmingDeletePanel] = useState<number>()

  return (
    <>
      <ol className="list-group list-group-numbered">
        {value.map((element, i) => {
          return (
            <li
              key={i}
              className="list-group-item d-flex align-items-center"
            >
              <div className="flex-grow-1 text-truncate ms-2">
                {getElementLabel(element)}
              </div>
              <div className="flex-shrink-0">
                <IconButton
                  aria-label="Move this element before the previous one"
                  className="ms-2"
                  disabled={readOnly || i === 0}
                  icon={faChevronUp}
                  variant="light"
                  onClick={() => {
                    onChange([
                      ...value.slice(0, i - 1),
                      value[i],
                      value[i - 1],
                      ...value.slice(i + 1)
                    ])
                  }}
                />
                <IconButton
                  aria-label="Move this element after the next one"
                  className="ms-2"
                  disabled={readOnly || i === value.length - 1}
                  icon={faChevronDown}
                  variant="light"
                  onClick={() => {
                    onChange([
                      ...value.slice(0, i),
                      value[i + 1],
                      value[i],
                      ...value.slice(i + 2)
                    ])
                  }}
                />

                <IconButton
                  aria-label="Delete this element"
                  className="ms-2"
                  disabled={readOnly}
                  icon={faTimes}
                  variant="light"
                  onClick={() => {
                    const elementToDelete = value[i]
                    if (
                      'type' in elementToDelete && elementToDelete.type === 'panel'
                      && elementToDelete.elements.length > 0
                    ) {
                      setConfirmingDeletePanel(i)
                    } else {
                      onChange([
                        ...value.slice(0, i),
                        ...value.slice(i + 1)
                      ])
                    }
                  }}
                />
              </div>
            </li>
          )
        })}
      </ol>

      {confirmingDeletePanel !== undefined && (
        <DeletePanelConfirmationModal
          panel={value[confirmingDeletePanel] as FormPanel}
          onConfirm={({ deleteContents }) => {
            setConfirmingDeletePanel(undefined)
            const indexOfPanelToDelete = confirmingDeletePanel
            if (deleteContents) {
              // Delete the panel.
              onChange([
                ...value.slice(0, indexOfPanelToDelete),
                ...value.slice(indexOfPanelToDelete + 1)
              ])
            } else {
              // Delete the panel, but put its contents in its place.
              onChange([
                ...value.slice(0, indexOfPanelToDelete),
                // Cast as T[] isn't great, but is valid for how this component is used.
                // T is either FormElement when this is used for elements on a page or
                // HtmlElement | Question when this used for elements in a panel.
                // FormPanel['elements'] is (HtmlElement | Question)[], so the cast
                // is valid in either case.
                ...(value[indexOfPanelToDelete] as FormPanel).elements as T[],
                ...value.slice(indexOfPanelToDelete + 1)
              ])
            }
          }}
          onDismiss={() => {
            setConfirmingDeletePanel(undefined)
          }}
        />
      )}
    </>
  )
}
