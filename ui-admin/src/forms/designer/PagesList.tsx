import { faChevronDown, faChevronUp, faTimes } from '@fortawesome/free-solid-svg-icons'
import React from 'react'

import { FormContent } from '@juniper/ui-core'

import { IconButton, Button } from 'components/forms/Button'

type PagesListProps = {
  formContent: FormContent
  readOnly: boolean
  onChange: (newValue: FormContent) => void
  setSelectedElementPath: (path: string) => void
}

/** UI for re-ordering pages in a form. */
export const PagesList = (props: PagesListProps) => {
  const { formContent, readOnly, onChange, setSelectedElementPath } = props
  const { pages } = formContent

  return (
    <ol className="list-group list-group">
      {pages.map((page, i) => {
        return (
          <li
            key={i}
            className="list-group-item d-flex align-items-center"
          >
            <div className="flex-grow-1 text-truncate ms-2">
              <Button variant={'secondary'}
                onClick={() => setSelectedElementPath(`pages[${i}]`)}>Page {i + 1}</Button>
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
              <IconButton
                aria-label="Delete this page"
                className="ms-2"
                disabled={readOnly || pages.length === 1}
                icon={faTimes}
                variant="light"
                onClick={() => {
                  onChange({
                    ...formContent,
                    pages: [
                      ...pages.slice(0, i),
                      ...pages.slice(i + 1)
                    ]
                  })
                }}
              />
            </div>
          </li>
        )
      })}
    </ol>
  )
}
