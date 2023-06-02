import React, { useState } from 'react'

import { VersionedForm } from 'api/api'

import { SurveyCreatorComponent } from 'survey-creator-react'
import { useSurveyJSCreator } from '../../util/surveyJsUtils'

type SurveyEditorViewProps<T extends VersionedForm> = {
  currentForm: T
  readOnly?: boolean
  onCancel: () => void
  onSave: (update: { content: string }) => Promise<void>
}

/** renders a survey for editing/viewing using the surveyJS editor */
const SurveyEditorView = <T extends VersionedForm>(props: SurveyEditorViewProps<T>) => {
  const {
    currentForm,
    readOnly = false,
    onCancel,
    onSave
  } = props

  const [saving, setSaving] = useState(false)

  const { surveyJSCreator } = useSurveyJSCreator(currentForm, () => { /* noop */ })
  if (surveyJSCreator) {
    surveyJSCreator.readOnly = readOnly
  }


  /** when save is pressed, call the handling function, and then update the survey with the response */
  async function handleSave() {
    if (!surveyJSCreator || saving) {
      return
    }
    setSaving(true)
    try {
      await onSave({ content: surveyJSCreator.text })
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="SurveyView">
      <div className="d-flex p-2 align-items-center">
        <div className="d-flex flex-grow-1">
          <h5>{currentForm.name}
            <span className="detail me-2 ms-2">version {currentForm.version}</span>
          </h5>
        </div>
        {!readOnly && (
          <button className="btn btn-primary me-md-2" type="button" onClick={handleSave}>
            Save
          </button>
        )}
        <button className="btn btn-secondary" type="button" onClick={onCancel}>Cancel</button>
      </div>
      {surveyJSCreator && <SurveyCreatorComponent creator={surveyJSCreator} /> }
    </div>
  )
}

export default SurveyEditorView
