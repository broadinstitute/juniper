import React, { useState } from 'react'
import { Tab, Tabs } from 'react-bootstrap'

import { FormContent, VersionedForm } from '@juniper/ui-core'

import { FormDesigner } from './FormDesigner'
import { OnChangeFormContent } from './formEditorTypes'
import { FormContentJsonEditor } from './FormContentJsonEditor'
import { FormPreview } from './FormPreview'
import { validateFormContent } from './formContentValidation'
import ErrorBoundary from 'util/ErrorBoundary'

type FormContentEditorProps = {
  initialContent: string
  visibleVersionPreviews: VersionedForm[]
  readOnly: boolean
  onChange: OnChangeFormContent
}

// TODO: Add JSDoc
// eslint-disable-next-line jsdoc/require-jsdoc
export const FormContentEditor = (props: FormContentEditorProps) => {
  const { initialContent, visibleVersionPreviews, readOnly, onChange } = props

  const [activeTab, setActiveTab] = useState<string | null>('designer')
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
          disabled={activeTab !== 'designer' && !tabsEnabled}
          eventKey="designer"
          title="Designer"
        >
          <ErrorBoundary>
            <FormDesigner
              readOnly={readOnly}
              value={editedContent}
              onChange={newContent => {
                setEditedContent(newContent)
                try {
                  validateFormContent(newContent)
                  onChange(true, newContent)
                } catch (err) {
                  onChange(false, undefined)
                }
              }}
            />
          </ErrorBoundary>
        </Tab>
        <Tab
          disabled={activeTab !== 'json' && !tabsEnabled}
          eventKey="json"
          title="JSON Editor"
        >
          <ErrorBoundary>
            <FormContentJsonEditor
              initialValue={editedContent}
              readOnly={readOnly}
              onChange={(isValid, newContent) => {
                if (isValid) {
                  setEditedContent(newContent)
                  onChange(true, newContent)
                } else {
                  onChange(false, undefined)
                }
                setTabsEnabled(isValid)
              }}
            />
          </ErrorBoundary>
        </Tab>
        <Tab
          disabled={activeTab !== 'preview' && !tabsEnabled}
          eventKey="preview"
          title="Preview"
        >
          <ErrorBoundary>
            <FormPreview formContent={editedContent} />
          </ErrorBoundary>
        </Tab>
        { visibleVersionPreviews.map(form =>
          <Tab
            disabled={activeTab !== `preview${form.version}` && !tabsEnabled}
            key={`preview${form.version}`}
            eventKey={`preview${form.version}`}
            title={`Version ${form.version}`}
          >
            <FormPreview formContent={JSON.parse(form.content) as FormContent} />
          </Tab>
        )}
      </Tabs>
    </div>
  )
}
