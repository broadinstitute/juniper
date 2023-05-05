import React, { useState } from 'react'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import Modal from 'react-bootstrap/Modal'
import LoadingSpinner from 'util/LoadingSpinner'
import Api from 'api/api'
import { currentIsoDate } from 'util/timeUtils'
import { failureNotification } from 'util/notifications'
import { Store } from 'react-notifications-component'

const CreateDatasetModal = ({ studyEnvContext, show, setShow }: {studyEnvContext: StudyEnvContextT, show: boolean,
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
      <Modal.Title>Create Dataset</Modal.Title>
      <div className="ms-4">
        {studyEnvContext.study.name}: {studyEnvContext.currentEnv.environmentName}
      </div>
    </Modal.Header>
    <Modal.Body>
      <form onSubmit={e => e.preventDefault()}>
        <label className="form-label">
              Dataset Name <input type="text" className="form-control" value={'foo'}/>
        </label>
      </form>
    </Modal.Body>
    <Modal.Footer>
      <LoadingSpinner isLoading={isLoading}>
        <button className="btn btn-primary" onClick={doExport}>Create</button>
        <button className="btn btn-secondary" onClick={() => setShow(false)}>Cancel</button>
      </LoadingSpinner>
    </Modal.Footer>
  </Modal>
}

export default CreateDatasetModal
