import React, { useEffect, useState } from 'react'
import LoadingSpinner from 'util/LoadingSpinner'
import Api, {SiteContent, Survey} from 'api/api'
import Modal from 'react-bootstrap/Modal'
import {instantToDefaultString} from "util/timeUtils";
import Select from 'react-select'

/** component for selecting versions of a survey */
export default function SiteContentVersionSelector({ portalShortcode, stableId, updateVersion, onDismiss, current }:
                                            {portalShortcode: string, stableId: string, current: SiteContent,
                                                onDismiss: () => void,
                                                updateVersion: (version: number) => void}) {
    const [versionList, setVersionList] = useState<SiteContent[]>([])
    const [isLoading, setIsLoading] = useState(true)
    const [selectedVersion, setSelectedVersion] = useState<SiteContent>()
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

    return <Modal show={true} onHide={onDismiss}>
        <Modal.Header closeButton>
            <Modal.Title>Select version to edit</Modal.Title>
        </Modal.Header>
        <Modal.Body>
            <LoadingSpinner isLoading={isLoading}>
                <p>
                    You are currently editing <br/> Version <strong>{current.version}</strong>,
                    <span className="text-muted ms-2">created {instantToDefaultString(current.createdAt)}</span>.
                </p>
                <Select options={versionOpts} value={selectedOpt} onChange={opt =>
                setSelectedVersion(opt?.value)}/>
            </LoadingSpinner>
        </Modal.Body>
        <Modal.Footer>
            <button type="button" className="btn btn-secondary" onClick={onDismiss}>Cancel</button>
        </Modal.Footer>
    </Modal>
}
