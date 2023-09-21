import React, { useEffect, useId, useState } from 'react'
import LoadingSpinner from 'util/LoadingSpinner'
import Api, { PortalEnvironment, SiteContent } from 'api/api'
import Modal from 'react-bootstrap/Modal'
import { instantToDefaultString } from 'util/timeUtils'
import Select from 'react-select'
import { Button } from 'components/forms/Button'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faRotate } from '@fortawesome/free-solid-svg-icons'

type SiteContentVersionSelectorProps = {
  portalShortcode: string
  portalEnv: PortalEnvironment
  stableId: string
  current: SiteContent
  onDismiss: () => void
  loadSiteContent: (stableId: string, version: number) => void
  switchToVersion: (id: string, stableId: string, version: number) => void
}

/** component for selecting versions of a survey */
export default function SiteContentVersionSelector(props: SiteContentVersionSelectorProps) {
  const {
    portalShortcode, stableId, current, portalEnv, onDismiss,
    loadSiteContent, switchToVersion
  } = props
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

  const editSelectedVersion = () => {
    if (selectedVersion) {
      loadSiteContent(selectedVersion.stableId, selectedVersion.version)
      onDismiss()
    }
  }

  const doVersionSwitch = () => {
    if (selectedVersion) {
      switchToVersion(selectedVersion.id, selectedVersion.stableId, selectedVersion.version)
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
        <label htmlFor={selectId}>Select version</label>
        <Select inputId={selectId} options={versionOpts} value={selectedOpt} onChange={opt =>
          setSelectedVersion(opt?.value)}/>
      </LoadingSpinner>
    </Modal.Body>
    <Modal.Footer className="flex-column">
      {selectedVersion && <>
        <Button variant="primary" className="w-100" onClick={editSelectedVersion}>
                Edit version { selectedVersion.version }
        </Button>
        {portalEnv.environmentName === 'sandbox' &&
            <Button type="button" variant="secondary" onClick={doVersionSwitch}>
          Switch sandbox to version { selectedVersion.version } <FontAwesomeIcon icon={faRotate}/>
            </Button>}
      </>
      }
    </Modal.Footer>
  </Modal>
}
