import React, { useId, useState } from 'react'
import Modal from 'react-bootstrap/Modal'
import InfoPopup from 'components/forms/InfoPopup'
import LoadingSpinner from 'util/LoadingSpinner'
import Select from 'react-select'
import { instantToDefaultString, VersionedForm } from '@juniper/ui-core'
import { StudyEnvContextT, studyEnvFormsPath } from '../StudyEnvironmentRouter'
import { useLoadingEffect } from 'api/api-utils'
import Api from 'api/api'
import { Button } from 'components/forms/Button'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faArrowRightFromBracket } from '@fortawesome/free-solid-svg-icons'

/**
 *
 */
export default function FormHistoryModal({
  studyEnvContext, workingForm, visibleVersionPreviews, setVisibleVersionPreviews, isConsentForm = false, onDismiss
}:
  {studyEnvContext: StudyEnvContextT, workingForm: VersionedForm,
    visibleVersionPreviews: VersionedForm[],
    setVisibleVersionPreviews: (versions: VersionedForm[]) => void,
    isConsentForm?: boolean, onDismiss: () => void}) {
  const [versionList, setVersionList] = useState<VersionedForm[]>([])
  const [selectedVersion, setSelectedVersion] = useState<number>()
  const stableId = workingForm.stableId
  const selectId = useId()

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

  return <Modal show={true}
    onHide={() => {
      onDismiss()
    }}>
    <Modal.Header closeButton>
      <Modal.Title>{workingForm.name} - history</Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <LoadingSpinner isLoading={isLoading}>
        <div className="d-flex align-items-baseline">
          <label htmlFor={selectId} className="mt-3 d-block">Other versions</label>
          <InfoPopup content={<span>Viewing as a preview will open a new tab in the current editor.
        Opening in read-only mode will allow you to view the full editor in an entirely new browser tab.</span>}/>
        </div>
        <Select inputId={selectId} options={versionOpts} value={selectedOpt} onChange={opt =>
          setSelectedVersion(opt?.value)}/>
      </LoadingSpinner>
    </Modal.Body>
    <Modal.Footer>
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
      <a href={`${studyEnvFormsPath(studyEnvContext.portal.shortcode, studyEnvContext.study.shortcode,
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
    </Modal.Footer>
  </Modal>
}
