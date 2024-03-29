import React, { useState } from 'react'
import { Tab, Tabs } from 'react-bootstrap'

import { FormContent, PortalEnvironmentLanguage, VersionedForm } from '@juniper/ui-core'

import { FormDesigner } from './FormDesigner'
import { OnChangeFormContent } from './formEditorTypes'
import { FormContentJsonEditor } from './FormContentJsonEditor'
import { FormPreview } from './FormPreview'
import { validateFormContent } from './formContentValidation'
import ErrorBoundary from 'util/ErrorBoundary'
import { isEmpty } from 'lodash'
import useStateCallback from '../util/useStateCallback'

type FormContentEditorProps = {
  initialContent: string
  visibleVersionPreviews: VersionedForm[]
  supportedLanguages: PortalEnvironmentLanguage[]
  readOnly: boolean
  onChange: OnChangeFormContent
}

// TODO: Add JSDoc
// eslint-disable-next-line jsdoc/require-jsdoc
export const FormContentEditor = (props: FormContentEditorProps) => {
  const { initialContent, visibleVersionPreviews, supportedLanguages, readOnly, onChange } = props

  const [activeTab, setActiveTab] = useState<string | null>('designer')
  const [tabsEnabled, setTabsEnabled] = useState(true)

  const [editedContent, setEditedContent] = useStateCallback(() => JSON.parse(initialContent) as FormContent)

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
              content={editedContent}
              onChange={(newContent, callback?: () => void) => {
                setEditedContent(newContent, callback)
                try {
                  const errors = validateFormContent(newContent)
                  onChange(errors, newContent)
                } catch (err) {
                  //@ts-ignore
                  onChange([err.message], undefined)
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
              onChange={(validationErrors, newContent) => {
                if (isEmpty(validationErrors) && newContent) {
                  setEditedContent(newContent)
                  onChange(validationErrors, newContent)
                } else {
                  onChange(validationErrors, undefined)
                }
                setTabsEnabled(isEmpty(validationErrors))
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
            <FormPreview formContent={editedContent} supportedLanguages={supportedLanguages} />
          </ErrorBoundary>
        </Tab>
        { visibleVersionPreviews.map(form =>
          <Tab
            key={`preview${form.version}`}
            eventKey={`preview${form.version}`}
            title={`Version ${form.version}`}
          >
            <FormPreview
              formContent={JSON.parse(form.content) as FormContent}
              supportedLanguages={supportedLanguages}
            />
          </Tab>
        )}
      </Tabs>
    </div>
  )
}
