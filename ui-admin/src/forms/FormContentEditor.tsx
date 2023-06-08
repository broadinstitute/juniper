import React, { useState } from 'react'
import { Tab, Tabs } from 'react-bootstrap'

import { FormContent } from '@juniper/ui-core'

import { OnChangeFormContent } from './formEditorTypes'
import { FormContentJsonEditor } from './FormContentJsonEditor'
import { FormPreview } from './FormPreview'

type FormContentEditorProps = {
  initialContent: string
  readOnly: boolean
  onChange: OnChangeFormContent
}

export const FormContentEditor = (props: FormContentEditorProps) => {
  const { initialContent, readOnly, onChange } = props

  const [activeTab, setActiveTab] = useState<string | null>('json')
  const [tabsEnabled, setTabsEnabled] = useState(true)

  const [editedContent, setEditedContent] = useState(() => JSON.parse(initialContent) as FormContent)

  return (
    <div className="FormContentEditor d-flex flex-column flex-grow-1">
      <Tabs
        activeKey={activeTab ?? undefined}
        className="mb-1"
        mountOnEnter
        unmountOnExit
        onSelect={setActiveTab}
      >
        <Tab
          disabled={activeTab !== 'json' && !tabsEnabled}
          eventKey="json"
          title="JSON Editor"
        >
          <FormContentJsonEditor
            initialValue={editedContent}
            readOnly={readOnly}
            onChange={(isValid, newSurvey) => {
              if (isValid) {
                setEditedContent(newSurvey)
                onChange(true, newSurvey)
              } else {
                onChange(false, undefined)
              }
              setTabsEnabled(isValid)
            }}
          />
        </Tab>
        <Tab
          disabled={activeTab !== 'preview' && !tabsEnabled}
          eventKey="preview"
          title="Preview"
        >
          <FormPreview formContent={editedContent} />
        </Tab>
      </Tabs>
    </div>
  )
}
