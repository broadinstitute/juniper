import React, { useState } from 'react'
import { Tab, Tabs } from 'react-bootstrap'

import { JuniperSurvey } from '@juniper/ui-core'

import { OnChangeSurvey } from './surveyEditorTypes'
import { SurveyJsonEditor } from './SurveyJsonEditor'
import { SurveyPreview } from './SurveyPreview'

type SurveyEditorProps = {
  initialContent: string
  readOnly: boolean
  onChange: OnChangeSurvey
}

export const SurveyEditor = (props: SurveyEditorProps) => {
  const { initialContent, readOnly, onChange } = props

  const [activeTab, setActiveTab] = useState<string | null>('json')
  const [tabsEnabled, setTabsEnabled] = useState(true)

  const [editedSurvey, setEditedSurvey] = useState(() => JSON.parse(initialContent) as JuniperSurvey)

  return (
    <div className="SurveyEditor d-flex flex-column flex-grow-1">
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
          <SurveyJsonEditor
            initialValue={editedSurvey}
            readOnly={readOnly}
            onChange={(isValid, newSurvey) => {
              if (isValid) {
                setEditedSurvey(newSurvey)
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
          <SurveyPreview survey={editedSurvey} />
        </Tab>
      </Tabs>
    </div>
  )
}
