import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faChevronDown, faChevronUp } from '@fortawesome/free-solid-svg-icons'
import React from 'react'

import { FormElement } from '@juniper/ui-core'

import { getElementLabel } from './designer-utils'

type ElementListProps = {
  readOnly: boolean
  value: FormElement[]
  onChange: (newValue: FormElement[]) => void
}

/** UI for re-ordering a list of form elements. */
export const ElementList = (props: ElementListProps) => {
  const { readOnly, value, onChange } = props

  return (
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
              <button
                aria-disabled={readOnly}
                aria-label="Move this element before the previous one"
                className="btn btn-light ms-2"
                disabled={i === 0}
                onClick={() => {
                  if (!readOnly) {
                    onChange([
                      ...value.slice(0, i - 1),
                      value[i],
                      value[i - 1],
                      ...value.slice(i + 1)
                    ])
                  }
                }}
              >
                <FontAwesomeIcon icon={faChevronUp} />
              </button>
              <button
                aria-disabled={readOnly}
                aria-label="Move this element after the next one"
                className="btn btn-light ms-2"
                disabled={i === value.length - 1}
                onClick={() => {
                  if (!readOnly) {
                    onChange([
                      ...value.slice(0, i),
                      value[i + 1],
                      value[i],
                      ...value.slice(i + 2)
                    ])
                  }
                }}
              >
                <FontAwesomeIcon icon={faChevronDown} />
              </button>
            </div>
          </li>
        )
      })}
    </ol>
  )
}
