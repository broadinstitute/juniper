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

  const [selectedElementName, setSelectedElementName] = useState<string>()

  return (
    <div className="overflow-hidden flex-grow-1 d-flex flex-row mh-100" style={{ flexBasis: 0 }}>
      <div className="flex-shrink-0 border-end" style={{ width: 400, overflowY: 'scroll' }}>
        <FormTableOfContents
          formContent={value}
          selectedElementName={selectedElementName}
          onSelectElement={setSelectedElementName}
        />
      </div>
      <div className="flex-grow-1 overflow-scroll">
      </div>
    </div>
  )
}
