import React, { useState } from 'react'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import Modal from 'react-bootstrap/Modal'
import LoadingSpinner from 'util/LoadingSpinner'

const ExportDataControl = ({ studyEnvContext }: {studyEnvContext: StudyEnvContextT}) => {
  const [show, setShow] = useState(false)
  const [isLoading] = useState(false)

  const doExport = () => {
    alert('not yet implemented')
  }
  return <>
    <button className="btn btn-secondary" onClick={() => setShow(!show)} aria-label="show or hide export modal">
      Download
    </button>
    <Modal show={show} onHide={() => setShow(false)}>
      <Modal.Header closeButton>
        <Modal.Title>Export enrollee data - {studyEnvContext.study.name}</Modal.Title>
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
