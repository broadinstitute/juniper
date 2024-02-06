import React, { useState } from 'react'
import Modal from 'react-bootstrap/Modal'
import LoadingSpinner from 'util/LoadingSpinner'
import { ParticipantTask, StudyEnvironmentSurvey } from '@juniper/ui-core'
import { instantToDateString } from 'util/timeUtils'
import { doApiLoad, useLoadingEffect } from 'api/api-utils'
import { StudyEnvParams } from '../StudyEnvironmentRouter'
import Api, { ParticipantTaskUpdateDto } from 'api/api'
import { Button } from '../../components/forms/Button'
import InfoPopup from '../../components/forms/InfoPopup'
import { successNotification } from '../../util/notifications'
import { Store } from 'react-notifications-component'

export type SurveyEnvironmentDetailModalProps = {
  studyEnvParams: StudyEnvParams
  onDismiss: () => void
  stableId: string
}

/**
 * Shows details for a given environment's survey history
 */
export default function SurveyEnvironmentDetailModal(props: SurveyEnvironmentDetailModalProps) {
  const { onDismiss, stableId, studyEnvParams  } = props
  const [configuredSurveys, setConfiguredSurveys] = useState<StudyEnvironmentSurvey[]>([])
  const [participantTasks, setParticipantTasks] = useState<ParticipantTask[]>([])
  const { isLoading, reload } = useLoadingEffect(async () => {
    const configsResponse = await Api.findConfiguredSurveys(studyEnvParams.portalShortcode,
      studyEnvParams.studyShortcode,
      studyEnvParams.envName,
      undefined, stableId)
    setConfiguredSurveys(configsResponse.sort((a, b) => a.createdAt! - b.createdAt!))
    const tasksResponse = await Api.findTasksForStableId(studyEnvParams.portalShortcode,
      studyEnvParams.studyShortcode, studyEnvParams.envName, stableId)
    setParticipantTasks(tasksResponse)
  })

  const mostRecentVersion = configuredSurveys[configuredSurveys.length - 1]?.survey?.version

  const updateAllTasks = () => {
    doApiLoad(async () => {
      const updateObj: ParticipantTaskUpdateDto = {
        updateAll: true,
        updates: [
          { targetStableId: stableId, updateToVersion: mostRecentVersion }
        ]
      }
      await Api.updateParticipantTaskVersions(studyEnvParams.portalShortcode,
        studyEnvParams.studyShortcode,
        studyEnvParams.envName, updateObj)
      Store.addNotification(successNotification('Task versions updated'))
      reload()
    })
  }

  const hasParticipantsOnOldVersions = participantTasks.some(task => task.targetAssignedVersion !== mostRecentVersion)

  return <Modal show={true} onHide={onDismiss}>
    <LoadingSpinner isLoading={isLoading}>
      <Modal.Header closeButton>
        <Modal.Title>
          <div>{studyEnvParams.envName}</div>
          <span className="fst-italic fw-light">{configuredSurveys[0] && configuredSurveys[0].survey.name}</span>
        </Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <table className="table table-striped">
          <thead>
            <tr>
              <td></td>
              <td>launch date</td>
              <td># participants<br/> currently assigned</td>
            </tr>
          </thead>
          <tbody>
            {configuredSurveys.map((config, index) => {
              const numParticipants = participantTasks
                .filter(task => task.targetAssignedVersion === config.survey.version).length
              const isLastRow = config.survey.version === mostRecentVersion
              return <tr key={index}>
                <td className="align-middle">v{config.survey.version}</td>
                <td className="align-middle">{ instantToDateString(config.createdAt)}</td>
                <td className="align-middle">
                  <span>{ numParticipants }</span>
                  { (hasParticipantsOnOldVersions && isLastRow) &&
                    <span className="ms-4">
                      <Button variant="light" className="border m-1"
                        onClick={updateAllTasks}>
                    Update all to version {mostRecentVersion}
                      </Button>
                      <InfoPopup content={`Update all participants in the ${studyEnvParams.envName} 
                      environment to have the latest version of this form.  
                      This means when they revisit, they will see the latest version, with any prior
                      answers already filled-in`}/>
                    </span>
                  }
                </td>
              </tr>
            })}
          </tbody>
        </table>
      </Modal.Body>
      <Modal.Footer>
        <LoadingSpinner isLoading={isLoading}>
          <Button variant="secondary" onClick={() => { onDismiss() }}>Ok</Button>
        </LoadingSpinner>
      </Modal.Footer>
    </LoadingSpinner>
  </Modal>
}
