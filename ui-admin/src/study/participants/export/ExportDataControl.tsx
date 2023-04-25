import React, { useState } from 'react'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import Modal from 'react-bootstrap/Modal'
import LoadingSpinner from 'util/LoadingSpinner'

const ExportDataControl = ({ studyEnvContext }: {studyEnvContext: StudyEnvContextT}) => {
  const [show, setShow] = useState(false)
  const [humanReadable, setHumanReadable] = useState<boolean>(false)
  const [onlyIncludeMostRecent, setOnlyIncludeMostRecent] = useState<boolean>(false)
  const [fileFormat, setFileFormat] = useState<string>('TSV')

  const [isLoading] = useState(false)

  const doExport = () => {
    alert('not yet implemented')
  }
  const humanReadableChanged = (e: React.ChangeEvent<HTMLInputElement>) => {
    setHumanReadable(e.target.value === 'true')
  }
  const includeRecentChanged = (e: React.ChangeEvent<HTMLInputElement>) => {
    setOnlyIncludeMostRecent(e.target.value === 'true')
  }
  const fileFormatChanged = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFileFormat(e.target.value)
  }

  const downloadLink = `${studyEnvContext.currentEnvPath}/export/data/filename.zip`

  return <>
    <button className="btn btn-secondary" onClick={() => setShow(!show)} aria-label="show or hide export modal">
      Download
    </button>
    <Modal show={show} onHide={() => setShow(false)}>
      <Modal.Header closeButton>
        <Modal.Title>Export enrollee data - {studyEnvContext.study.name}</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <form onSubmit={e => e.preventDefault()}>
          <div className="py-2">
            <span className="fw-bold">Data format</span><br/>
            <label className="me-3">
              <input type="radio" name="humanReadable" value="false" checked={!humanReadable}
                onChange={humanReadableChanged} className="me-1"/> Human-readable
            </label>
            <label>
              <input type="radio" name="humanReadable" value="true" checked={humanReadable}
                onChange={humanReadableChanged} className="me-1"/> Analysis-friendly
            </label>
          </div>
          <div className="py-2">
            <span className="fw-bold">Completions included of a survey (for recurring surveys)</span><br/>
            <label className="me-3">
              <input type="radio" name="onlyIncludeMostRecent" value="false" checked={!onlyIncludeMostRecent}
                onChange={includeRecentChanged} className="me-1"/>
              Only include most recent
            </label>
            <label>
              <input type="radio" name="onlyIncludeMostRecent" value="true" checked={onlyIncludeMostRecent}
                onChange={includeRecentChanged} className="me-1"/>
              Include all completions
            </label>
          </div>
          <div className="py-2">
            <span className="fw-bold">File format</span><br/>
            <label className="me-3">
              <input type="radio" name="fileFormat" value="TSV" checked={fileFormat == 'TSV'}
                onChange={fileFormatChanged} className="me-1"/>
              Tab-delimited (.tsv)
            </label>
            <label>
              <input type="radio" name="fileFormat" value="XSLX" checked={fileFormat == 'XSLX'}
                onChange={fileFormatChanged} className="me-1"/>
              Excel (.xlsx)
            </label>
          </div>
        </form>
      </Modal.Body>
      <Modal.Footer>
        <LoadingSpinner isLoading={isLoading}>
          <a className="btn btn-primary" href={downloadLink} download>Download</a>
          <button className="btn btn-secondary" onClick={() => setShow(false)}>Cancel</button>
        </LoadingSpinner>
      </Modal.Footer>
    </Modal>
  </>
}

export default ExportDataControl
