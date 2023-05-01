import React, { useState } from 'react'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import Modal from 'react-bootstrap/Modal'
import LoadingSpinner from 'util/LoadingSpinner'
import Api from 'api/api'
import { currentIsoDate } from 'util/timeUtils'
import { failureNotification } from 'util/notifications'
import { Store } from 'react-notifications-component'

const ExportDataControl = ({ studyEnvContext, show, setShow }: {studyEnvContext: StudyEnvContextT, show: boolean,
                           setShow:  React.Dispatch<React.SetStateAction<boolean>>}) => {
  const [humanReadable, setHumanReadable] = useState(true)
  const [onlyIncludeMostRecent, setOnlyIncludeMostRecent] = useState(true)
  const [fileFormat, setFileFormat] = useState('TSV')

  const [isLoading, setIsLoading] = useState(false)

  const optionsFromState = () => {
    return {
      onlyIncludeMostRecent,
      splitOptionsIntoColumns: !humanReadable,
      stableIdsForOptions: !humanReadable,
      fileFormat
    }
  }

  const saveLoadedData = async (response: Response, fileName: string) => {
    if (!response.ok) {
      Store.addNotification(failureNotification('Export failed'))
      setIsLoading(false)
      return
    }
    const blob = await response.blob()
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = fileName
    a.click()
    setIsLoading(false)
  }

  const doExport = () => {
    setIsLoading(true)
    Api.exportEnrollees(studyEnvContext.portal.shortcode, studyEnvContext.study.shortcode,
      studyEnvContext.currentEnv.environmentName, optionsFromState()).then(response => {
      saveLoadedData(response, `${currentIsoDate()  }-enrollees.${fileFormat.toLowerCase()}`)
    })
  }

  const doDictionaryExport = () => {
    setIsLoading(true)
    Api.exportDictionary(studyEnvContext.portal.shortcode, studyEnvContext.study.shortcode,
      studyEnvContext.currentEnv.environmentName, optionsFromState()).then(response => {
      saveLoadedData(response, `${currentIsoDate()  }-DataDictionary.xlsx`)
    })
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

  return <Modal show={show} onHide={() => setShow(false)}>
    <Modal.Header closeButton>
      <Modal.Title>Download</Modal.Title>
      <div className="ms-4">
        {studyEnvContext.study.name}: {studyEnvContext.currentEnv.environmentName}
      </div>
    </Modal.Header>
    <Modal.Body>
      <form onSubmit={e => e.preventDefault()}>
        <div className="py-2">
          <span className="fw-bold">Data format</span><br/>
          <label className="me-3">
            <input type="radio" name="humanReadable" value="true" checked={humanReadable}
              onChange={humanReadableChanged} className="me-1" disabled={true}/> Human-readable
          </label>
          <label>
            <input type="radio" name="humanReadable" value="false" checked={!humanReadable}
              onChange={humanReadableChanged} className="me-1" disabled={true}/> Analysis-friendly
          </label>
        </div>
        <div className="py-2">
          <span className="fw-bold">Completions included of a survey (for recurring surveys)</span><br/>
          <label className="me-3">
            <input type="radio" name="onlyIncludeMostRecent" value="true" checked={onlyIncludeMostRecent}
              onChange={includeRecentChanged} className="me-1" disabled={true}/>
            Only include most recent
          </label>
          <label>
            <input type="radio" name="onlyIncludeMostRecent" value="false" checked={!onlyIncludeMostRecent}
              onChange={includeRecentChanged} className="me-1" disabled={true}/>
            Include all completions
          </label>
        </div>
        <div className="py-2">
          <span className="fw-bold">File format</span><br/>
          <label className="me-3">
            <input type="radio" name="fileFormat" value="TSV" checked={fileFormat == 'TSV'}
              onChange={fileFormatChanged} className="me-1" disabled={true}/>
            Tab-delimited (.tsv)
          </label>
          <label>
            <input type="radio" name="fileFormat" value="XLSX" checked={fileFormat == 'EXCEL'}
              onChange={fileFormatChanged} className="me-1" disabled={true}/>
            Excel (.xlsx)
          </label>
        </div>
      </form>
    </Modal.Body>
    <Modal.Footer>
      <LoadingSpinner isLoading={isLoading}>
        <button className="btn btn-primary" onClick={doExport}>Download</button>
        <button className="btn btn-secondary" onClick={doDictionaryExport}>Download dictionary (.xlsx)</button>
        <button className="btn btn-secondary" onClick={() => setShow(false)}>Cancel</button>
      </LoadingSpinner>
    </Modal.Footer>
  </Modal>
}

export default ExportDataControl
