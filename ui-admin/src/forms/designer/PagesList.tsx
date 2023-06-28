import { faChevronDown, faChevronUp } from '@fortawesome/free-solid-svg-icons'
import React from 'react'

import { FormContent } from '@juniper/ui-core'

import { IconButton } from 'components/forms/Button'

type PagesListProps = {
  formContent: FormContent
  readOnly: boolean
  onChange: (newValue: FormContent) => void
}

/** UI for re-ordering pages in a form. */
export const PagesList = (props: PagesListProps) => {
  const { formContent, readOnly, onChange } = props
  const { pages } = formContent

  return (
    <ol className="list-group list-group-numbered">
      {pages.map((page, i) => {
        return (
          <li
            key={i}
            className="list-group-item d-flex align-items-center"
          >
            <div className="flex-grow-1 text-truncate ms-2">
              Page {i + 1}
            </div>
            <div className="flex-shrink-0">
              <IconButton
                aria-label="Move this page before the previous one"
                className="ms-2"
                disabled={readOnly || i === 0}
                icon={faChevronUp}
                variant="light"
                onClick={() => {
                  if (!readOnly) {
                    onChange({
                      ...formContent,
                      pages: [
                        ...pages.slice(0, i - 1),
                        pages[i],
                        pages[i - 1],
                        ...pages.slice(i + 1)
                      ]
                    })
                  }
                }}
              />
              <IconButton
                aria-label="Move this page after the next one"
                className="ms-2"
                disabled={readOnly || i === pages.length - 1}
                icon={faChevronDown}
                variant="light"
                onClick={() => {
                  if (!readOnly) {
                    onChange({
                      ...formContent,
                      pages: [
                        ...pages.slice(0, i),
                        pages[i + 1],
                        pages[i],
                        ...pages.slice(i + 2)
                      ]
                    })
                  }
                }}
              />
            </div>
          </li>
        )
      })}
    </ol>
  )
}
