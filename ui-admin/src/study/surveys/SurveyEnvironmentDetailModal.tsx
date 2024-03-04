import React, { useMemo, useState } from 'react'
import Modal from 'react-bootstrap/Modal'
import LoadingSpinner from 'util/LoadingSpinner'
import { ParticipantTask, StudyEnvironmentSurvey, instantToDateString } from '@juniper/ui-core'
import { doApiLoad, useLoadingEffect } from 'api/api-utils'
import { StudyEnvParams } from '../StudyEnvironmentRouter'
import Api, { ParticipantTaskUpdateDto } from 'api/api'
import { Button } from 'components/forms/Button'
import { successNotification } from 'util/notifications'
import { Store } from 'react-notifications-component'
import { ColumnDef, getCoreRowModel, getSortedRowModel, useReactTable } from '@tanstack/react-table'
import InfoPopup from 'components/forms/InfoPopup'
import { basicTableLayout } from 'util/tableUtils'

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

  const columns = useMemo((): ColumnDef<StudyEnvironmentSurvey>[] => {
    return [{
      id: 'versionName',
      header: '',
      cell: ({ row }) => row.original.survey.version
    }, {
      id: 'launchDate',
      header: 'Launch date',
      cell: ({ row }) => instantToDateString(row.original.createdAt)
    }, {
      id: 'numParticipants',
      header: '# participants currently assigned',
      cell: ({ row }) => participantTasks
        .filter(task => task.targetAssignedVersion === row.original.survey.version).length
    }, {
      id: 'actions',
      header: '',
      cell: ({ row }) => <>
        { (hasParticipantsOnOldVersions && row.original.survey.version === mostRecentVersion) &&
          <span className="ms-4">
            <Button variant="secondary" outline={true} onClick={updateAllTasks}>
              Update all to version {mostRecentVersion}
            </Button>
            <InfoPopup content={`Update all participants in the ${studyEnvParams.envName} 
            environment to have the latest version of this form.  
            This means when they revisit, they will see the latest version, with any prior
            answers already filled-in`}/>
          </span>
        }
      </>
    }]
  }, [stableId, participantTasks.length])

  const table = useReactTable({
    data: configuredSurveys,
    columns,
    enableRowSelection: true,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel()
  })

  const assignSurveyTasks = () => {
    doApiLoad(async () => {
      const newTasks = await Api.assignParticipantTasksToEnrollees(studyEnvParams, {
        assignAllUnassigned: true,
        targetStableId: stableId,
        targetAssignedVersion: mostRecentVersion,
        taskType: 'SURVEY'
      })
      Store.addNotification(successNotification(`Assigned survey to ${newTasks.length} enrollees`))
      reload()
    })
  }

  const updateAllTasks = () => {
    doApiLoad(async () => {
      const updateObj: ParticipantTaskUpdateDto = {
        updateAll: true,
        updates: [
          { targetStableId: stableId, updateToVersion: mostRecentVersion }
        ]
      }
      await Api.updateParticipantTaskVersions(studyEnvParams, updateObj)
      Store.addNotification(successNotification('Task versions updated'))
      reload()
    })
  }

  const surveyName = (configuredSurveys[0] && configuredSurveys[0].survey.name) ?? ''
  const hasParticipantsOnOldVersions = participantTasks.some(task => task.targetAssignedVersion !== mostRecentVersion)

  return <Modal show={true} onHide={onDismiss} className="modal-lg">
    <LoadingSpinner isLoading={isLoading}>
      <Modal.Header closeButton>
        <Modal.Title>
          <div>{studyEnvParams.envName}</div>
          <span className="fst-italic fw-light">{surveyName}</span>
        </Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <LoadingSpinner isLoading={isLoading}>
          { basicTableLayout(table, { tableClass: 'table table-striped align-middle' })}
          <div className="d-flex">
            <Button variant="secondary" outline={true} onClick={assignSurveyTasks}>
            Assign to {studyEnvParams.envName} participants
            </Button>
            <InfoPopup content={`Assign ${surveyName} to all participants in the ${studyEnvParams.envName} 
              environment who are eligible.`}/>
          </div>
        </LoadingSpinner>
      </Modal.Body>
      <Modal.Footer>
        <LoadingSpinner isLoading={isLoading}>
          <Button variant="secondary" onClick={() => { onDismiss() }}>Ok</Button>
        </LoadingSpinner>
      </Modal.Footer>
    </LoadingSpinner>
  </Modal>
}
