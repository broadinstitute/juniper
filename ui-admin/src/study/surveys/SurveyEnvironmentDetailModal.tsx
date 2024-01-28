import React, { useState } from 'react'
import Modal from 'react-bootstrap/Modal'
import LoadingSpinner from 'util/LoadingSpinner'
import { EnvironmentName, StudyEnvironmentSurveyNamed } from '@juniper/ui-core'
import { instantToDateString } from 'util/timeUtils'

export type SurveyEnvironmentDetailModalProps = {
  onDismiss: () => void
  stableId: string
  envName: EnvironmentName
  configuredSurveys: StudyEnvironmentSurveyNamed[]
}

/**
 * Shows details for a given environment's survey history
 */
export default function SurveyEnvironmentDetailModal(props: SurveyEnvironmentDetailModalProps) {
  const { onDismiss, stableId, configuredSurveys, envName } = props
  const [isLoading, setIsLoading] = useState(false)

  const envConfigs = configuredSurveys
    .filter(config => config.envName === envName && config.survey.stableId === stableId)

  return <Modal show={true} onHide={onDismiss}>
    <Modal.Header closeButton>
      <Modal.Title>
        {envConfigs[0].survey.name}<br/>
        <span className="fst-italic">{envName}</span>
      </Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <table>
        <tbody>
          {envConfigs.map(config => <tr>
            <td>v{config.survey.version}</td>
            <td>{ instantToDateString(config.createdAt)}</td>
          </tr>)}
        </tbody>
      </table>

    </Modal.Body>
    <Modal.Footer>
      <LoadingSpinner isLoading={isLoading}>
        <button className="btn btn-secondary" onClick={() => {
          onDismiss()
        }}>Cancel</button>
      </LoadingSpinner>
    </Modal.Footer>
  </Modal>
}

/**

envName === 'sandbox') &&  <div className="nav-item dropdown ms-1">
  <IconButton icon={faEllipsisH}  data-bs-toggle="dropdown"
              aria-expanded="false" aria-label="configure survey menu"/>
  <div className="dropdown-menu">
    <ul className="list-unstyled">
      <li>
        <button className="dropdown-item"
                onClick={() => {
                  setShowArchiveSurveyModal(!showArchiveSurveyModal)
                  setSelectedSurveyConfig(envConfig)
                }}>
          Archive
        </button>
      </li>
      <li className="pt-2">
        <button className="dropdown-item"
                onClick={() => {
                  setShowDeleteSurveyModal(!showDeleteSurveyModal)
                  setSelectedSurveyConfig(envConfig)
                }}>
          Delete
        </button>
      </li>
    </ul>
  </div>
</div> } */
