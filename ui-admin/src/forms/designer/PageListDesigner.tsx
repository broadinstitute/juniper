import { concat } from 'lodash/fp'
import React from 'react'

import { FormContent } from '@juniper/ui-core'

import { PagesList } from './PagesList'
import { Button } from 'components/forms/Button'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faPlus } from '@fortawesome/free-solid-svg-icons'

type PageListDesignerProps = {
    formContent: FormContent
    readOnly: boolean
    onChange: (newValue: FormContent) => void
}

/** UI for editing pages in a form. */
export const PageListDesigner = (props: PageListDesignerProps) => {
  const { formContent, readOnly, onChange } = props
  const { pages = [] } = formContent

  const newPage = () => ({ elements: [] })

  return (
    <>
      <h2>Pages</h2>
      <div className="mb-3">
        <Button
          disabled={readOnly}
          tooltip="Add a new page."
          variant="secondary"
          onClick={() => {
            onChange({
              ...formContent,
              pages: concat(pages, newPage())
            })
          }}
        >
          <FontAwesomeIcon icon={faPlus}/> Add page
        </Button>
      </div>

      {pages.length === 0
        ? (
          <p>This form does not contain any pages.</p>
        ) : (
          <PagesList
            formContent={formContent}
            readOnly={readOnly}
            onChange={onChange}
          />
        )}
    </>
  )
}
