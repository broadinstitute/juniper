import React, { useEffect, useId, useState } from 'react'
import LoadingSpinner from 'util/LoadingSpinner'
import Api, { VersionedForm } from 'api/api'
import Modal from 'react-bootstrap/Modal'
import { Button } from 'components/forms/Button'
import { instantToDefaultString } from 'util/timeUtils'
import Select from 'react-select'
import { failureNotification } from 'util/notifications'
import { Store } from 'react-notifications-component'

/** component for selecting versions of a form */
export default function VersionSelector({
  portalShortcode, stableId, show, setShow,
  visibleVersionPreviews, setVisibleVersionPreviews
}:
                                          {portalShortcode: string, stableId: string,
                                            show: boolean, setShow: (show: boolean) => void
                                            visibleVersionPreviews: VersionedForm[],
                                            setVisibleVersionPreviews: (versions: VersionedForm[]) => void}) {
  const [versionList, setVersionList] = useState<VersionedForm[]>([])
  const [selectedVersion, setSelectedVersion] = useState<number>()
  const [isLoading, setIsLoading] = useState(true)
  const selectId = useId()
  useEffect(() => {
    setIsLoading(true)
    Api.getSurveyVersions(portalShortcode, stableId).then(result => {
      setVersionList(result.sort((a, b) => b.version - a.version))
      setIsLoading(false)
    }).catch(() => {
      Store.addNotification(failureNotification('Error loading form history'))
      setShow(false)
      setIsLoading(false)
    })
  }, [])

  function loadVersion(version: number) {
    Api.getSurvey(portalShortcode, stableId, version).then(result => {
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
        <p>Selecting a previous form version will allow you to preview it in a new tab of the form editor.</p>
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
