import React, { useState } from 'react'
import Modal from 'react-bootstrap/Modal'
import LoadingSpinner from 'util/LoadingSpinner'
import Select from 'react-select'
import { instantToDefaultString, Survey, VersionedForm } from '@juniper/ui-core'
import { StudyEnvContextT, studyEnvFormsPath } from '../StudyEnvironmentRouter'
import { useLoadingEffect } from 'api/api-utils'
import Api from 'api/api'
import { Button } from 'components/forms/Button'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faArrowRightFromBracket, faRotate } from '@fortawesome/free-solid-svg-icons'
import useReactSingleSelect from '../../util/react-select-utils'

/**
 * Shows past versions of a form and controls for slecting them
 */
export default function FormHistoryModal({
  studyEnvContext, workingForm, onDismiss, replaceSurvey
}:
  {studyEnvContext: StudyEnvContextT, workingForm: VersionedForm,
    replaceSurvey: (survey: Survey) => void, onDismiss: () => void}) {
  const [versionList, setVersionList] = useState<Survey[]>([])
  const [selectedVersion, setSelectedVersion] = useState<Survey>()
  const stableId = workingForm.stableId

  const { isLoading } = useLoadingEffect(async () => {
    const result = await Api.getSurveyVersions(studyEnvContext.portal.shortcode, stableId)
    setVersionList(
      result.sort((a, b) => b.version - a.version)
        .filter(s => s.version !== workingForm.version)
    )
  }, [stableId])

  const doVersionSwitch = async () => {
    if (selectedVersion) {
      await replaceSurvey(selectedVersion)
      onDismiss()
    }
  }

  const {  onChange, options, selectedOption, selectInputId } = useReactSingleSelect(versionList, formVersion => ({
    label: <span>
            Version <strong>{ formVersion.version }</strong>
      <span className="text-muted fst-italic ms-2">
                ({instantToDefaultString(formVersion.createdAt)})
      </span>
    </span>,
    value: formVersion
  }), setSelectedVersion, selectedVersion)

  return <Modal show={true}
    onHide={() => {
      onDismiss()
    }}>
    <Modal.Header closeButton>
      <Modal.Title>
        <h2 className="h4">Version history</h2>
        <span className="fs-5">{workingForm.name}</span>
      </Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <LoadingSpinner isLoading={isLoading}>
        <div className="d-flex align-items-baseline">
          <label htmlFor={selectInputId} className="mt-3 d-block">Other versions</label>
        </div>
        <Select inputId={selectInputId} options={options} value={selectedOption} onChange={onChange}/>
      </LoadingSpinner>
    </Modal.Body>
    <Modal.Footer className="d-flex justify-content-center">
      {selectedVersion && <div className="d-flex flex-column w-100">
        <a href={`${studyEnvFormsPath(studyEnvContext.portal.shortcode, studyEnvContext.study.shortcode,
          studyEnvContext.currentEnv.environmentName
        )}/surveys/${stableId}/${selectedVersion.version}`}
        className="btn btn-primary"
        aria-disabled={!selectedVersion}
        style={{ pointerEvents: selectedVersion ? undefined : 'none' }}
        onClick={onDismiss}
        target="_blank"
        >
            View/Edit version {selectedVersion.version} <FontAwesomeIcon icon={faArrowRightFromBracket}/>
        </a>
        {studyEnvContext.currentEnv.environmentName === 'sandbox' &&
            <Button type="button" variant="link" onClick={doVersionSwitch} outline={true}>
              Switch sandbox to version { selectedVersion.version } <FontAwesomeIcon icon={faRotate}/>
            </Button>}
      </div>
      }
    </Modal.Footer>
  </Modal>
}
