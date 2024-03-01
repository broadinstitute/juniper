import React, { useState } from 'react'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import Modal from 'react-bootstrap/Modal'
import LoadingSpinner from 'util/LoadingSpinner'
import Api from 'api/api'
import { currentIsoDate } from '@juniper/ui-core'
import { Link } from 'react-router-dom'
import { saveBlobAsDownload } from 'util/downloadUtils'
import { doApiLoad } from 'api/api-utils'

const FILE_FORMATS = [{
  label: 'Tab-delimted (.tsv)',
  value: 'TSV',
  fileSuffix: 'tsv'
}, {
  label: 'Excel (.xlsx)',
  value: 'EXCEL',
  fileSuffix: 'xlsx'
}]

/** form for configuring and downloading enrollee data */
const ExportDataControl = ({ studyEnvContext, show, setShow }: {studyEnvContext: StudyEnvContextT, show: boolean,
                           setShow:  React.Dispatch<React.SetStateAction<boolean>>}) => {
  const [humanReadable, setHumanReadable] = useState(true)
  const [onlyIncludeMostRecent, setOnlyIncludeMostRecent] = useState(true)
  const [fileFormat, setFileFormat] = useState(FILE_FORMATS[0])

  const [isLoading, setIsLoading] = useState(false)

  const optionsFromState = () => {
    return {
      onlyIncludeMostRecent,
      splitOptionsIntoColumns: !humanReadable,
      stableIdsForOptions: !humanReadable,
      fileFormat: fileFormat.value
    }
  }

  const doExport = () => {
    doApiLoad(async () => {
      const response = await Api.exportEnrollees(studyEnvContext.portal.shortcode, studyEnvContext.study.shortcode,
        studyEnvContext.currentEnv.environmentName, optionsFromState())
      const fileName = `${currentIsoDate()}-enrollees.${fileFormat.fileSuffix}`
      const blob = await response.blob()
      saveBlobAsDownload(blob, fileName)
    }, { setIsLoading })
  }

  const doDictionaryExport = () => {
    doApiLoad(async () => {
      const response = await Api.exportDictionary(studyEnvContext.portal.shortcode, studyEnvContext.study.shortcode,
        studyEnvContext.currentEnv.environmentName, optionsFromState())
      const fileName = `${currentIsoDate()}-DataDictionary.xlsx`
      const blob = await response.blob()
      saveBlobAsDownload(blob, fileName)
    }, { setIsLoading })
  }

  const humanReadableChanged = (e: React.ChangeEvent<HTMLInputElement>) => {
    setHumanReadable(e.target.value === 'true')
  }
  const includeRecentChanged = (e: React.ChangeEvent<HTMLInputElement>) => {
    setOnlyIncludeMostRecent(e.target.value === 'true')
  }

  return <Modal show={show} onHide={() => setShow(false)}>
    <Modal.Header closeButton>
      <Modal.Title>
        Download
      </Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <form onSubmit={e => e.preventDefault()}>
        <div className="py-2">
          <span className="fw-bold">Data format</span><br/>
          <label className="me-3">
            <input type="radio" name="humanReadable" value="true" checked={humanReadable}
              onChange={humanReadableChanged} className="me-1"/> Human-readable
          </label>
          <label>
            <input type="radio" name="humanReadable" value="false" checked={!humanReadable}
              onChange={humanReadableChanged} className="me-1"/> Analysis-friendly
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
          {FILE_FORMATS.map(format => <label className="me-3" key={format.value}>
            <input type="radio" name="fileFormat" value="TSV" checked={fileFormat.value === format.value}
              onChange={() => setFileFormat(format)}
              className="me-1"/>
            {format.label}
          </label>)}
        </div>
        <hr/>
        <div>
          For more information about download formats,
          see the <Link to="https://broad-juniper.zendesk.com/hc/en-us/articles/18259824756123" target="_blank">
          help page</Link>.
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
