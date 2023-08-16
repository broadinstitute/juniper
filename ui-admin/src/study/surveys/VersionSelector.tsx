import React, { useEffect, useState } from 'react'
import LoadingSpinner from 'util/LoadingSpinner'
import Api, { Survey, VersionedForm } from 'api/api'
import Modal from 'react-bootstrap/Modal'
import { Button } from '../../components/forms/Button'

/** component for selecting versions of a survey */
export default function VersionSelector({
  portalShortcode, stableId, show, setShow,
  previewedVersions, setPreviewedVersions
}:
                                          {portalShortcode: string, stableId: string,
                                            show: boolean, setShow: (show: boolean) => void
                                            previewedVersions: VersionedForm[],
                                            setPreviewedVersions: (versions: VersionedForm[]) => void}) {
  const [versionList, setVersionList] = useState<Survey[]>([])
  const [selectedVersion, setSelectedVersion] = useState<number>()
  const [isLoading, setIsLoading] = useState(true)
  useEffect(() => {
    Api.getSurveyVersions(portalShortcode, stableId).then(result => {
      setVersionList(result.sort((a, b) => b.version - a.version))
      setIsLoading(false)
    })
  }, [])

  function loadVersion(version: number) {
    Api.getSurvey(portalShortcode, stableId, version).then(result => {
      setPreviewedVersions([...previewedVersions, result])
    })
  }

  return <Modal show={show} onHide={() => setShow(false)}>
    <Modal.Header closeButton>
      <Modal.Title>View a previous version</Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <p>Selecting a previous survey version will allow you to preview it in a new tab of the survey editor.</p>
      <LoadingSpinner isLoading={isLoading}>
        <select
          className="form-control"
          id="form-preview-version"
          value={selectedVersion}
          onChange={e => {
            setSelectedVersion(parseInt(e.target.value))
          }}
        >
          {versionList.map(version => (
            <option key={version.version} value={version.version}>
              {version.version}
            </option>
          ))}
        </select>
      </LoadingSpinner>
    </Modal.Body>
    <Modal.Footer>
      <Button
        variant="primary"
        onClick={() => {
          if (selectedVersion) {
            loadVersion(selectedVersion)
          }
          setShow(false)
        }}
      >
        Open version
      </Button>
      <Button
        variant="secondary"
        onClick={() => setShow(false)}
      >
        Cancel
      </Button>
    </Modal.Footer>
  </Modal>
}
