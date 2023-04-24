import React, { useState } from 'react'
import { getExportDataBrowserPath, StudyEnvContextT } from '../../StudyEnvironmentRouter'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faColumns, faDownload } from '@fortawesome/free-solid-svg-icons'
import Modal from 'react-bootstrap/Modal'
import LoadingSpinner from '../../../util/LoadingSpinner'
import { useNavigate } from 'react-router-dom'

const ExportDataControl = ({ studyEnvContext }: {studyEnvContext: StudyEnvContextT}) => {
  const [show, setShow] = useState(false)
  const [isDownload, setIsDownload] = useState(false)
  const [isLoading, setIsLoading] = useState(false)
  const navigate = useNavigate()

  const doExport = () => {
    if (!isDownload) {
      navigate(getExportDataBrowserPath(studyEnvContext.currentEnvPath))
      return
    }
  }
  return <>
    <button className="btn btn-secondary" onClick={() => setShow(!show)} aria-label="show or hide export modal">
      Export <FontAwesomeIcon icon={faDownload} className="fa-lg"/>
    </button>
    <Modal show={show} onHide={() => setShow(false)}>
      <Modal.Header closeButton>
        <Modal.Title>Export enrollee data</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        (Some export options will go here)
      </Modal.Body>
      <Modal.Footer>
        <LoadingSpinner isLoading={isLoading}>
          <button className="btn btn-primary" onClick={doExport}>Export</button>
          <button className="btn btn-secondary" onClick={() => setShow(false)}>Cancel</button>
        </LoadingSpinner>
      </Modal.Footer>
    </Modal>
  </>
}

export default ExportDataControl
