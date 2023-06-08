import classNames from 'classnames'
import React, { useState } from 'react'

import { VersionedForm } from 'api/api'

import { FormContentEditor } from 'forms/FormContentEditor'

type SurveyEditorViewProps = {
  currentForm: VersionedForm
  readOnly?: boolean
  onCancel: () => void
  onSave: (update: { content: string }) => Promise<void>
}

/** renders a survey for editing/viewing */
const SurveyEditorView = (props: SurveyEditorViewProps) => {
  const {
    currentForm,
    readOnly = false,
    onCancel,
    onSave
  } = props

  const [editedContent, setEditedContent] = useState<string>()
  const [isEditorValid, setIsEditorValid] = useState(true)
  const [saving, setSaving] = useState(false)
  const isSaveEnabled = !!editedContent && isEditorValid && !saving

  const onClickSave = async () => {
    if (!isSaveEnabled) {
      return
    }
    setSaving(true)
    try {
      await onSave({ content: editedContent })
      setEditedContent(undefined)
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="SurveyView d-flex flex-column flex-grow-1 mx-1 mb-1">
      <div className="d-flex p-2 align-items-center">
        <div className="d-flex flex-grow-1">
          <h5>{currentForm.name}
            <span className="detail me-2 ms-2">version {currentForm.version}</span>
          </h5>
        </div>
        {!readOnly && (
          <button
            aria-disabled={isSaveEnabled ? 'true' : 'false'}
            className={classNames(
              'btn btn-primary me-md-2',
              { disabled: !isSaveEnabled }
            )}
            type="button"
            onClick={onClickSave}
          >
            Save
          </button>
        )}
        <button className="btn btn-secondary" type="button" onClick={onCancel}>Cancel</button>
      </div>
      <FormContentEditor
        initialContent={currentForm.content}
        readOnly={readOnly}
        onChange={(isValid, newContent) => {
          if (isValid) {
            setEditedContent(JSON.stringify(newContent))
          }
          setIsEditorValid(isValid)
        }}
      />
    </div>
  )
}

export default SurveyEditorView
