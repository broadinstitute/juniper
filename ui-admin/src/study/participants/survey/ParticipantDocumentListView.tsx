import React from 'react'
import {
  ColumnDef,
  getCoreRowModel,
  Row,
  useReactTable
} from '@tanstack/react-table'
import {
  Enrollee,
  ParticipantFile,
  saveBlobAsDownload
} from '@juniper/ui-core'
import {
  basicTableLayout,
  renderEmptyMessage
} from 'util/table/tableUtils'
import { createdAtColumn } from 'util/table/tableColumnUtils'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import {
  faCheck,
  faDownload,
  faSpinner,
  faX
} from '@fortawesome/free-solid-svg-icons'
import Api from 'api/api'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import { NavLink } from 'react-router-dom'
import { useUser } from 'user/UserProvider'
import InfoPopup from 'components/forms/InfoPopup'

export const ParticipantDocumentListView = ({
  studyEnvContext,
  enrollee,
  showAssociatedTasks,
  documents
}: {
  studyEnvContext: StudyEnvContextT,
  enrollee: Enrollee,
  showAssociatedTasks: boolean,
  documents: ParticipantFile[]
}) => {
  const { user } = useUser()

  // @ts-ignore
  const columns: ColumnDef<ParticipantFile>[] = [
    {
      ...createdAtColumn(),
      header: 'Uploaded At'
    },
    {
      header: 'File Name',
      accessorKey: 'fileName'
    },
    ...(showAssociatedTasks ? [{
      header: 'Associated Tasks',
      accessorKey: 'surveyResponseIds',
      cell: ({ row }: { row: Row<ParticipantFile> }) => {
        return surveyResponseIdsToTaskNames(
          enrollee, row.original.associatedAnswers.map(answer => answer.surveyResponseId!)
        )
      }
    }] : []),
    {
      header: 'File Type',
      accessorKey: 'fileType'
    },
    {
      header: 'Antivirus Result',
      accessorKey: 'virusScanResult',
      cell: ({ row }) => {
        switch (row.original.virusScanResult) {
          case 'CLEAN':
            return <>
              <FontAwesomeIcon
                icon={faCheck}/>
              <InfoPopup content={'No malware detected'}/>
            </>
          case 'QUARANTINED':
            return <>
              <FontAwesomeIcon icon={faX}/>
              <InfoPopup content={
                'Malware detected on file. If you believe this to be incorrect, please contact Juniper staff.'
              }/>
            </>
          case 'UNSCANNED':
            return <>
              <FontAwesomeIcon icon={faSpinner}/>
              <InfoPopup content={
                'Scan in-progress or could not be completed. Please check back later.'
              }/>
            </>
        }

        return <span>(no result)</span>
      }
    },
    {
      header: 'Actions',
      cell: ({ row }) => {
        return <button
          className='btn btn-secondary'
          onClick={() => download(row.original)}
          disabled={row.original.virusScanResult === 'QUARANTINED'}
        >
          <FontAwesomeIcon icon={faDownload}/>
        </button>
      }
    }
  ]

  if (user?.superuser) {
    columns.push({
      header: 'External ID',
      cell: ({ row }) => {
        return row.original.externalFileId
      }
    })
  }

  const table = useReactTable({
    columns,
    data: documents,
    getCoreRowModel: getCoreRowModel()
  })


  const download = async (file: ParticipantFile) => {
    const response = await Api.downloadParticipantFile(
      studyEnvContext.portal.shortcode,
      studyEnvContext.study.shortcode,
      studyEnvContext.currentEnv.environmentName,
      enrollee.shortcode,
      file.fileName
    )

    saveBlobAsDownload(await response.blob(), file.fileName)
  }

  return <>
    <span className="fs-5 fw-bold">
      Document Uploads
    </span>
    {basicTableLayout(table)}
    { renderEmptyMessage(documents, 'This participant has not uploaded any documents') }
  </>
}

const surveyResponseIdsToTaskNames = (
  enrollee: Enrollee, surveyResponseIds: string[]
) => {
  const associatedTasks = surveyResponseIds.map(surveyResponseId => {
    return enrollee.participantTasks.find(task => task.surveyResponseId === surveyResponseId)
  }).filter(task => task !== undefined)

  if (associatedTasks.length === 0) {
    return <div className={'fst-italic text-muted'}>This document is not associated with any tasks</div>
  }

  return (
    <ul className={'ps-3'}>
      {associatedTasks.map(task =>
        <li key={task?.id}>
          <NavLink to={`../surveys/${task?.targetStableId}${task?.id ? `?taskId=${task.id}` : ''}`}>
            {task?.targetName}
          </NavLink>
        </li>
      )}
    </ul>
  )
}
