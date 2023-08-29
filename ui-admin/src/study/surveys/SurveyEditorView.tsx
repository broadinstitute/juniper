import React, { useState } from 'react'

import { VersionedForm } from 'api/api'

import { Button } from 'components/forms/Button'
import { FormContentEditor } from 'forms/FormContentEditor'
import LoadedLocalDraftModal from 'forms/designer/modals/LoadedLocalDraftModal'
import DiscardLocalDraftModal from 'forms/designer/modals/DiscardLocalDraftModal'
import { deleteDraft, FormDraft, getDraft, getFormDraftKey, saveDraft } from 'forms/designer/utils/formDraftUtils'
import { useAutosaveEffect } from '@juniper/ui-core/build/autoSaveUtils'
import { faClockRotateLeft, faExclamationCircle } from '@fortawesome/free-solid-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import VersionSelector from './VersionSelector'
import { StudyEnvContextT } from '../StudyEnvironmentRouter'
import { isEmpty } from 'lodash'

type SurveyEditorViewProps = {
  studyEnvContext: StudyEnvContextT
  currentForm: VersionedForm
  readOnly?: boolean
  onCancel: () => void
  onSave: (update: { content: string }) => Promise<void>
}

/** renders a survey for editing/viewing */
const SurveyEditorView = (props: SurveyEditorViewProps) => {
  const {
    studyEnvContext,
    currentForm,
    readOnly = false,
    onCancel,
    onSave
  } = props

  const FORM_DRAFT_KEY = getFormDraftKey({ form: currentForm })
  const FORM_DRAFT_SAVE_INTERVAL = 10000

  const [saving, setSaving] = useState(false)
  const [savingDraft, setSavingDraft] = useState(false)

  //Let the user know if we loaded a draft from local storage when the component first renders. It's important
  //for them to know if the version of the survey they're seeing are from a draft or are actually published.
  const [showLoadedDraftModal, setShowLoadedDraftModal] = useState(!!getDraft({ formDraftKey: FORM_DRAFT_KEY }))
  const [showDiscardDraftModal, setShowDiscardDraftModal] = useState(false)
  const [showVersionSelector, setShowVersionSelector] = useState(false)
  const [visibleVersionPreviews, setVisibleVersionPreviews] = useState<VersionedForm[]>([])
  const [showErrors, setShowErrors] = useState(false)

  const [draft, setDraft] = useState<FormDraft | undefined>(
    !readOnly ? getDraft({ formDraftKey: FORM_DRAFT_KEY }) : undefined)

  const [validationErrors, setValidationErrors] = useState<string[]>([])
  const isSaveEnabled = !!draft && isEmpty(validationErrors) && !saving

  const saveDraftToLocalStorage = () => {
    setDraft(currentDraft => {
      if (currentDraft && currentDraft?.content !== getDraft({ formDraftKey: FORM_DRAFT_KEY })?.content) {
        saveDraft({
          formDraftKey: FORM_DRAFT_KEY,
          draft: currentDraft,
          setSavingDraft
        })
      }
      return currentDraft
    })
  }

  useAutosaveEffect(saveDraftToLocalStorage, FORM_DRAFT_SAVE_INTERVAL)

  const onClickSave = async () => {
    if (!isSaveEnabled) {
      return
    }
    setSaving(true)
    try {
      await onSave({ content: draft?.content })
      //Once we've persisted the form draft to the database, there's no need to keep it in local storage.
      //Future drafts will have different FORM_DRAFT_KEYs anyway, as they're based on the form version number.
      deleteDraft({ formDraftKey: FORM_DRAFT_KEY })
      setDraft(undefined)
    } finally {
      setSaving(false)
    }
  }

  const onClickCancel = () => {
    if (draft) {
      setShowDiscardDraftModal(true)
    } else {
      onCancel()
    }
  }

  return (
    <div className="SurveyView d-flex flex-column flex-grow-1 mx-1 mb-1">
      <div className="d-flex p-2 align-items-center">
        <div className="d-flex flex-grow-1">
          <h5>{currentForm.name}
            <span className="detail me-2 ms-2">version {currentForm.version}</span>
            <button className="btn btn-secondary" onClick={() => setShowVersionSelector(true)}>
              <FontAwesomeIcon icon={faClockRotateLeft}/> History
            </button>
          </h5>
        </div>
        { savingDraft && <span className="detail me-2 ms-2">Saving draft...</span> }
        { validationErrors && !isEmpty(validationErrors) &&
            <div className="position-relative ms-auto me-2 ms-2">
              <button className="btn btn-outline-danger"
                onClick={() => setShowErrors(!showErrors)} aria-label="view errors">
                    View errors <FontAwesomeIcon icon={faExclamationCircle} className="fa-lg"/>
              </button>
              { showErrors && <div className="position-absolute border border-gray rounded bg-white p-3"
                style={{ width: '750px', right: 0 }}>
                <div className="border-b border-black">
                  <label>
                    <span>The following error(s) were found in your survey:</span>
                  </label>
                </div>
                <hr/>
                { !isEmpty(validationErrors) && validationErrors.map((error, index) => {
                  return (
                    <li key={`error-${index}`}>{error}</li>
                  )
                })}
              </div> }
            </div>
        }
        {!readOnly && (
          <Button
            disabled={!isSaveEnabled}
            className="me-md-2"
            tooltip={(() => {
              if (!isEmpty(validationErrors)) {
                return 'Please correct errors before saving.'
              }
              if (!draft) {
                return 'Form is unchanged. Make changes to save.'
              }
              return 'Save changes'
            })()}
            tooltipPlacement={'bottom'}
            variant={isEmpty(validationErrors) ? 'primary' : 'danger'}
            onClick={onClickSave}
          >
            Save
          </Button>
        )}
        <button className="btn btn-secondary" type="button"
          onClick={onClickCancel}>Cancel</button>
        { showLoadedDraftModal && draft &&
            <LoadedLocalDraftModal
              onDismiss={() => setShowLoadedDraftModal(false)}
              lastUpdated={draft?.date}/>}
        { showDiscardDraftModal && draft &&
            <DiscardLocalDraftModal
              formDraftKey={FORM_DRAFT_KEY}
              onExit={() => onCancel()}
              onSaveDraft={() =>
                saveDraft({
                  formDraftKey: FORM_DRAFT_KEY,
                  draft,
                  setSavingDraft
                })}
              onDismiss={() => setShowDiscardDraftModal(false)}
            />}
        { showVersionSelector && <VersionSelector
          studyEnvContext={studyEnvContext}
          visibleVersionPreviews={visibleVersionPreviews}
          setVisibleVersionPreviews={setVisibleVersionPreviews}
          stableId={currentForm.stableId}
          setShow={setShowVersionSelector}
          show={showVersionSelector}/>
        }
      </div>
      <FormContentEditor
        initialContent={draft?.content || currentForm.content} //favor loading the draft, if we find one
        visibleVersionPreviews={visibleVersionPreviews}
        readOnly={readOnly}
        onChange={(newValidationErrors, newContent) => {
          setShowErrors(false)
          if (isEmpty(newValidationErrors)) {
            setDraft({ content: JSON.stringify(newContent), date: Date.now() })
          }
          setValidationErrors(newValidationErrors)
        }}
      />
    </div>
  )
}

export default SurveyEditorView
