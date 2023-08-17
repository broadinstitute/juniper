import React, { useEffect, useId, useState } from 'react'
import LoadingSpinner from 'util/LoadingSpinner'
import Api, { SiteContent } from 'api/api'
import Modal from 'react-bootstrap/Modal'
import { instantToDefaultString } from 'util/timeUtils'
import Select from 'react-select'

type SiteContentVersionSelectorProps = {
    portalShortcode: string
    stableId: string
    current: SiteContent
    onDismiss: () => void
    loadSiteContent: (stableId: string, version: number) => void
}

/** component for selecting versions of a survey */
export default function SiteContentVersionSelector(props: SiteContentVersionSelectorProps) {
  const { portalShortcode, stableId, current, onDismiss, loadSiteContent } = props
  const [versionList, setVersionList] = useState<SiteContent[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [selectedVersion, setSelectedVersion] = useState<SiteContent>()
  const selectId = useId()
  const versionOpts = versionList.map(content => ({
    label: <span>
            Version <strong>{ content.version }</strong>
      <span className="text-muted fst-italic ms-2">
                ({instantToDefaultString(content.createdAt)})
      </span>
    </span>,
    value: content
  }))
  const selectedOpt = versionOpts
    .find(opt => opt.value.version === selectedVersion?.version)
  useEffect(() => {
    Api.getSiteContentVersions(portalShortcode, stableId).then(result => {
      setVersionList(result)
      setIsLoading(false)
    })
  }, [])

  const selectVersion = () => {
    if (selectedVersion) {
      loadSiteContent(selectedVersion.stableId, selectedVersion.version)
      onDismiss()
    }
  }

  return <Modal show={true} onHide={onDismiss}>
    <Modal.Header closeButton>
      <Modal.Title>Site content version</Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <LoadingSpinner isLoading={isLoading}>
        <p className="text-muted">
          <span>You are currently editing version {current.version},</span>
          <br/>
          <span>created {instantToDefaultString(current.createdAt)}</span>.
        </p>
        <label htmlFor={selectId}>Select version to edit</label>
        <Select inputId={selectId} options={versionOpts} value={selectedOpt} onChange={opt =>
          setSelectedVersion(opt?.value)}/>

      </LoadingSpinner>
    </Modal.Body>
    <Modal.Footer>
      {selectedVersion && <button type="button" className="btn btn-primary" onClick={selectVersion}>
                Edit version { selectedVersion.version }
      </button> }
      <button type="button" className="btn btn-secondary" onClick={onDismiss}>Cancel</button>
    </Modal.Footer>
  </Modal>
}
