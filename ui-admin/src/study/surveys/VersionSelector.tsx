import React, { useId, useState } from 'react'
import LoadingSpinner from 'util/LoadingSpinner'
import Api, { VersionedForm } from 'api/api'
import Modal from 'react-bootstrap/Modal'
import { Button } from 'components/forms/Button'
import { instantToDefaultString } from 'util/timeUtils'
import Select from 'react-select'
import { faArrowRightFromBracket } from '@fortawesome/free-solid-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { StudyEnvContextT, studyEnvFormsPath } from '../StudyEnvironmentRouter'
import { useLoadingEffect } from '../../api/api-utils'

/** component for selecting versions of a form */
export default function VersionSelector({
  studyEnvContext, stableId, show, setShow,
  visibleVersionPreviews, setVisibleVersionPreviews
}:
                                          {studyEnvContext: StudyEnvContextT, stableId: string,
                                            show: boolean, setShow: (show: boolean) => void
                                            visibleVersionPreviews: VersionedForm[],
                                            setVisibleVersionPreviews: (versions: VersionedForm[]) => void}) {
  const [versionList, setVersionList] = useState<VersionedForm[]>([])
  const [selectedVersion, setSelectedVersion] = useState<number>()
  const selectId = useId()

  const { isLoading } = useLoadingEffect(async () => {
    const result = await Api.getSurveyVersions(studyEnvContext.portal.shortcode, stableId)
    setVersionList(result.sort((a, b) => b.version - a.version))
  }, [stableId])

  function loadVersion(version: number) {
    Api.getSurvey(studyEnvContext.portal.shortcode, stableId, version).then(result => {
      setVisibleVersionPreviews([...visibleVersionPreviews, result])
    })
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

  return <Modal show={show} onHide={() => setShow(false)}>
    <Modal.Header closeButton>
      <Modal.Title>View a previous version</Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <LoadingSpinner isLoading={isLoading}>
        <p>Viewing as a preview will open a new tab in the current editor.
        Opening in read-only mode will allow you to view the full editor in an entirely new browser tab.</p>
        <label htmlFor={selectId}>Select version to preview</label>
        <Select inputId={selectId} options={versionOpts} value={selectedOpt} onChange={opt =>
          setSelectedVersion(opt?.value)}/>
      </LoadingSpinner>
    </Modal.Body>
    <Modal.Footer>
      <Button
        variant="primary"
        disabled={!selectedVersion}
        onClick={() => {
          if (selectedVersion) {
            loadVersion(selectedVersion)
          }
          setShow(false)
        }}
      >
        View preview
      </Button>
      <a href={`${studyEnvFormsPath(
        studyEnvContext.portal.shortcode,
        studyEnvContext.study.shortcode,
        studyEnvContext.currentEnv.environmentName
      )}/surveys/${stableId}/${selectedVersion}?readOnly=true`}
      className="btn btn-secondary"
      aria-disabled={!selectedVersion}
      style={{ pointerEvents: selectedVersion ? undefined : 'none' }}
      onClick={() => setShow(false)}
      target="_blank"
      >
        Open read-only editor <FontAwesomeIcon icon={faArrowRightFromBracket}/>
      </a>
      <Button
        variant="secondary"
        onClick={() => setShow(false)}
      >
        Cancel
      </Button>
    </Modal.Footer>
  </Modal>
}
