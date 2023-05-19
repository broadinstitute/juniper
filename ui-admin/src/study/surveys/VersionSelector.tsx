import React, { useEffect, useState } from 'react'
import LoadingSpinner from 'util/LoadingSpinner'
import Api, { Survey } from 'api/api'
import Modal from 'react-bootstrap/Modal'

/** component for selecting versions of a survey */
export default function VersionSelector({ studyShortname, stableId, updateVersion, show, setShow }:
                                          {studyShortname: string, stableId: string,
                                            show: boolean, setShow: (show: boolean) => void,
                                            updateVersion: (version: number) => void}) {
  const [versionList, setVersionList] = useState<Survey[]>([])
  const [isLoading, setIsLoading] = useState(true)
  useEffect(() => {
    Api.getSurveyVersions(studyShortname, stableId).then(result => {
      setVersionList(result)
      setIsLoading(false)
    })
  }, [])

  return <Modal show={show} onHide={() => setShow(false)}>
    <Modal.Header closeButton>
      <Modal.Title>Select a version</Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <LoadingSpinner isLoading={isLoading}>
        <ul>
          { versionList.map(survey => {
            return <li key={survey.version}>
              <button className="btn btn-secondary" onClick={() => updateVersion(survey.version)}>
                {survey.version} {/*<span className="detail">({survey.createdAt?.substring(0, 16)})</span>*/}
              </button>
            </li>
          })}
        </ul>
      </LoadingSpinner>
    </Modal.Body>
    <Modal.Footer>
      <button type="button" className="btn btn-secondary" onClick={() => setShow(false)}>Cancel</button>
    </Modal.Footer>
  </Modal>
}
