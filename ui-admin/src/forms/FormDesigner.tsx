import React, { useState } from 'react'

import { FormContent } from '@juniper/ui-core'

import { FormTableOfContents } from './FormTableOfContents'

type FormDesignerProps = {
  readOnly?: boolean
  value: FormContent
  onChange: (editedContent: FormContent) => void
}

/** UI for editing forms. */
export const FormDesigner = (props: FormDesignerProps) => {
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
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
      <div className="flex-grow-1 overflow-scroll">
        {(() => {
          if (selectedElementPath === undefined) {
            return (
              <p className="mt-5 text-center">Select an element to edit</p>
            )
          }

          return null
        })()}
      </div>
    </div>
  )
}
