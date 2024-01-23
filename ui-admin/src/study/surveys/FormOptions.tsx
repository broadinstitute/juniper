import React, { useId, useState } from 'react'
import LoadingSpinner from 'util/LoadingSpinner'
import Api, { Survey, VersionedForm } from 'api/api'
import Modal from 'react-bootstrap/Modal'
import { Button } from 'components/forms/Button'
import { instantToDefaultString } from 'util/timeUtils'
import Select from 'react-select'
import { faArrowRightFromBracket, faDownload } from '@fortawesome/free-solid-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { StudyEnvContextT, studyEnvFormsPath } from '../StudyEnvironmentRouter'
import { useLoadingEffect } from 'api/api-utils'
import { saveBlobAsDownload } from 'util/downloadUtils'

/** component for selecting versions of a form */
export default function FormOptions({
  studyEnvContext, form, isDirty, onDismiss,
  visibleVersionPreviews, setVisibleVersionPreviews, isConsentForm = false
}:
                                          {studyEnvContext: StudyEnvContextT, form: VersionedForm,
                                            isDirty: boolean, onDismiss: () => void
                                            visibleVersionPreviews: VersionedForm[],
                                            setVisibleVersionPreviews: (versions: VersionedForm[]) => void,
                                          isConsentForm?: boolean}) {
  const [versionList, setVersionList] = useState<VersionedForm[]>([])
  const [selectedVersion, setSelectedVersion] = useState<number>()
  const [showVersionHistory, setShowVersionHistory] = useState(false)
  const selectId = useId()
  const stableId = form.stableId

  const { isLoading } = useLoadingEffect(async () => {
    let result
    if (isConsentForm) {
      result = await Api.getConsentFormVersions(studyEnvContext.portal.shortcode, stableId)
    } else {
      result = await Api.getSurveyVersions(studyEnvContext.portal.shortcode, stableId)
    }
    setVersionList(result.sort((a, b) => b.version - a.version))
  }, [stableId])

  async function loadVersion(version: number) {
    let result
    if (isConsentForm) {
      result = await Api.getConsentForm(studyEnvContext.portal.shortcode, stableId, version)
    } else {
      result = await Api.getSurvey(studyEnvContext.portal.shortcode, stableId, version)
    }
    setVisibleVersionPreviews([...visibleVersionPreviews, result])
  }

  const versionOpts = versionList.map(formVersion => ({
    label: <span>
            Version <strong>{ formVersion.version }</strong>
      <span className="text-muted fst-italic ms-2">
                ({instantToDefaultString(formVersion.createdAt)})
      </span>
    </span>,
    value: formVersion.version
  })).filter(opt => visibleVersionPreviews.every(previewedVersion => previewedVersion.version !== opt.value))

  const selectedOpt = versionOpts.find(opt => opt.value === selectedVersion)
  const downloadJSON = () => {
    const content = form.content
    // To get this formatted nicely and not as one giant line, need to parse
    // this as an object and then stringify the result.
    const blob = new Blob(
      [JSON.stringify(JSON.parse(content), null, 2)],
      { type: 'application/json' })
    const filename = `${stableId}_v${form.version}${isDirty ? '_draft' : ''}.json`
    saveBlobAsDownload(blob, filename)
  }
  const isSurvey = !!(form as Survey).surveyType

  return <Modal show={true} onHide={onDismiss}>
    <Modal.Header closeButton>
      <Modal.Title>Form options</Modal.Title>
    </Modal.Header>
    <Modal.Body>
      { isSurvey &&
        <label className="form-label">
          Is required
          <input type="checkbox" checked={(form as Survey).required}
          />
        </label>
      }

      <LoadingSpinner isLoading={isLoading}>
        <label htmlFor={selectId}>Other versions</label>
        <Select inputId={selectId} options={versionOpts} value={selectedOpt} onChange={opt =>
          setSelectedVersion(opt?.value)}/>
        <div className="flex ps-4 pt-2">
          <Button
            variant="secondary"
            disabled={!selectedVersion}
            onClick={() => {
              if (selectedVersion) {
                loadVersion(selectedVersion)
              }
              onDismiss()
            }}
          >
            View preview
          </Button>
          <a href={`${studyEnvFormsPath(
            studyEnvContext.portal.shortcode,
            studyEnvContext.study.shortcode,
            studyEnvContext.currentEnv.environmentName
          )}/${isConsentForm ? 'consentForms' : 'surveys'}/${stableId}/${selectedVersion}?readOnly=true`}
          className="btn btn-secondary"
          aria-disabled={!selectedVersion}
          style={{ pointerEvents: selectedVersion ? undefined : 'none' }}
          onClick={onDismiss}
          target="_blank"
          >
            Open read-only editor <FontAwesomeIcon icon={faArrowRightFromBracket}/>
          </a>
          <p className="fst-italic fw-light">(Viewing as a preview will open a new tab in the current editor.
            Opening in read-only mode will allow you to view the full editor in an entirely new browser tab.)</p>
        </div>
      </LoadingSpinner>
      <Button variant="secondary" title="Download the current contents of the JSON Editor as a file"
        onClick={downloadJSON}>
        <FontAwesomeIcon icon={faDownload}/> Download JSON
      </Button>
    </Modal.Body>
    <Modal.Footer>
      <Button
        variant="secondary"
        onClick={onDismiss}
      >
        Cancel
      </Button>
    </Modal.Footer>
  </Modal>
}
