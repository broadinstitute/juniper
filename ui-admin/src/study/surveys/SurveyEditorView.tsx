import React, { useState } from 'react'

import { PortalEnvironment, Survey, VersionedForm } from 'api/api'

import { Button, EllipsisDropdownButton } from 'components/forms/Button'
import { FormContentEditor } from 'forms/FormContentEditor'
import LoadedLocalDraftModal from 'forms/designer/modals/LoadedLocalDraftModal'
import DiscardLocalDraftModal from 'forms/designer/modals/DiscardLocalDraftModal'
import { deleteDraft, FormDraft, getDraft, getFormDraftKey, saveDraft } from 'forms/designer/utils/formDraftUtils'
import { useAutosaveEffect } from '@juniper/ui-core/build/autoSaveUtils'
import { faExclamationCircle } from '@fortawesome/free-solid-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import FormOptions from './FormOptionsModal'
import { StudyEnvContextT } from '../StudyEnvironmentRouter'
import { isEmpty } from 'lodash'
import { SaveableFormProps } from './SurveyView'
import { ApiProvider } from '@juniper/ui-core'
import { previewApi } from 'util/apiContextUtils'
import { saveBlobAsDownload } from '../../util/downloadUtils'
import FormHistoryModal from './FormHistoryModal'

type SurveyEditorViewProps = {
  studyEnvContext: StudyEnvContextT
  currentForm: VersionedForm
  readOnly?: boolean
  onCancel: () => void
  onSave: (update: SaveableFormProps) => Promise<void>
}

/** renders a survey for editing/viewing */
const SurveyEditorView = (props: SurveyEditorViewProps) => {
  const {
    studyEnvContext,
    studyEnvContext: { portal, currentEnv },
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
  const [showAdvancedOptions, setShowAdvancedOptions] = useState(false)
  const [showVersionSelector, setShowVersionSelector] = useState(false)
  const [visibleVersionPreviews, setVisibleVersionPreviews] = useState<VersionedForm[]>([])
  const [showErrors, setShowErrors] = useState(false)

  const [draft, setDraft] = useState<FormDraft | undefined>(
    !readOnly ? getDraft({ formDraftKey: FORM_DRAFT_KEY }) : undefined)

  const [validationErrors, setValidationErrors] = useState<string[]>([])
  const isSaveEnabled = !!draft && isEmpty(validationErrors) && !saving
  const isSurvey = !!(currentForm as Survey).surveyType
  const portalEnv = portal.portalEnvironments.find((env: PortalEnvironment) =>
    env.environmentName === currentEnv.environmentName)

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
      await onSave(draft)
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

  const downloadJSON = () => {
    const content = draft ? draft.content : currentForm.content
    // To get this formatted nicely and not as one giant line, need to parse
    // this as an object and then stringify the result.
    const blob = new Blob(
      [JSON.stringify(JSON.parse(content), null, 2)],
      { type: 'application/json' })
    const filename = `${currentForm.stableId}_v${currentForm.version}${ draft ? '_draft' : ''}.json`
    saveBlobAsDownload(blob, filename)
  }

  return (
    <div className="SurveyView d-flex flex-column flex-grow-1 mx-1 mb-1">
      <div className="d-flex p-2 align-items-center">
        <div className="d-flex flex-grow-1">
          <h5>{currentForm.name}
            <span className="fs-6 text-muted fst-italic me-2 ms-2">
              (v{currentForm.version}
              { currentForm.publishedVersion && <span className="ms-1">
                - published v{currentForm.publishedVersion}
              </span> }
              )
            </span>
          </h5>
        </div>
        { savingDraft && <span className="detail me-2 ms-2">Saving draft...</span> }
        { !isEmpty(validationErrors) &&
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
              if (!draft) {
                return 'Form is unchanged. Make changes to save.'
              }
              if (!isEmpty(validationErrors)) {
                return 'Form is invalid. Correct to save.'
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
        <EllipsisDropdownButton aria-label="form options menu" className="ms-auto"/>
        <div className="dropdown-menu">
          <ul className="list-unstyled pt-3">
            { isSurvey && <li>
              <button className="dropdown-item"
                onClick={() => setShowAdvancedOptions(true)}>
                Configuration
              </button>
            </li> }
            <li>
              <button className="dropdown-item"
                onClick={() => setShowVersionSelector(true)}>
                Version History
              </button>
            </li>
            <li>
              <button className="dropdown-item"
                onClick={downloadJSON}>
                Download form JSON
              </button>
            </li>
          </ul>
        </div>
        { showVersionSelector && <FormHistoryModal
          studyEnvContext={studyEnvContext} visibleVersionPreviews={visibleVersionPreviews}
          setVisibleVersionPreviews={setVisibleVersionPreviews}
          workingForm={{ ...currentForm, ...draft }}
          isConsentForm={!isSurvey}
          onDismiss={() => setShowVersionSelector(false)}
        />}
        { showAdvancedOptions && <FormOptions
          workingForm={{ ...currentForm, ...draft }}
          updateWorkingForm={(props: SaveableFormProps) => {
            setDraft({ ...draft, ...props, date: Date.now() })
          }}
          onDismiss={() => setShowAdvancedOptions(false)}/>
        }
      </div>
      <ApiProvider api={previewApi(portal.shortcode, currentEnv.environmentName)}>
        <FormContentEditor
          initialContent={draft?.content || currentForm.content} //favor loading the draft, if we find one
          visibleVersionPreviews={visibleVersionPreviews}
          supportedLanguages={portalEnv?.supportedLanguages || []}
          readOnly={readOnly}
          onChange={(newValidationErrors, newContent) => {
            if (isEmpty(newValidationErrors)) {
              setShowErrors(false)
              setDraft({ ...draft, content: JSON.stringify(newContent), date: Date.now() })
            }
            setValidationErrors(newValidationErrors)
          }}
        />
      </ApiProvider>
    </div>
  )
}

export default SurveyEditorView
