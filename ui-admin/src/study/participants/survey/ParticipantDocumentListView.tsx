import React from 'react'
import {
  ColumnDef,
  getCoreRowModel,
  Row,
  useReactTable
} from '@tanstack/react-table'
import {
  DownloadRecord,
  Enrollee,
  instantToDefaultString,
  ParticipantFile,
  saveBlobAsDownload
} from '@juniper/ui-core'
import {
  basicTableLayout,
  renderEmptyMessage
} from 'util/table/tableUtils'
import { createdAtColumn } from 'util/table/tableColumnUtils'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faDownload, faTrash } from '@fortawesome/free-solid-svg-icons'
import Api from 'api/api'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import { NavLink } from 'react-router-dom'
import { useUser } from 'user/UserProvider'
import { Store } from 'react-notifications-component'
import { failureNotification, successNotification } from 'util/notifications'

export const ParticipantDocumentListView = ({
  studyEnvContext,
  enrollee,
  showAssociatedTasks,
  documents,
  onDocumentDeleted
}: {
  studyEnvContext: StudyEnvContextT,
  enrollee: Enrollee,
  showAssociatedTasks: boolean,
  documents: ParticipantFile[],
  onDocumentDeleted: () => void
}) => {
  const { user } = useUser()

  // @ts-ignore
  const columns: ColumnDef<ParticipantFile>[] = [
    {
      header: 'File Name',
      accessorKey: 'fileName'
    },
    {
      ...createdAtColumn(),
      header: 'Uploaded At'
    },
    {
      header: 'Downloads',
      cell: ({ row }) => <DownloadRecordList records={row.original.downloads ?? []}/>
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
        const result = row.original.virusScanResult
        if (result === 'CLEAN') {
          return <span>Clean</span>
        } else if (result === 'QUARANTINED') {
          return <span className='text-danger fw-bold'>Malware Detected</span>
        } else if (result === 'UNSCANNED') {
          return <span>In Progress</span>
        }

        return <span>(no result)</span>
      }
    },
    {
      header: 'ID',
      accessorKey: 'id'
    },
    {
      header: 'Actions',
      cell: ({ row }) => {
        return <div className='d-flex gap-2'>
          <button
            className='btn btn-secondary'
            onClick={() => download(row.original)}
            disabled={row.original.virusScanResult === 'QUARANTINED'}
          >
            <FontAwesomeIcon icon={faDownload}/>
          </button>
          <button
            className='btn btn-outline-danger border-0'
            onClick={() => deleteFile(row.original)}
          >
            <FontAwesomeIcon icon={faTrash}/>
          </button>
        </div>
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


  const deleteFile = async (file: ParticipantFile) => {
    if (!confirm(`Delete "${file.fileName}"? This cannot be undone.`)) return
    try {
      await Api.deleteParticipantFile(
        studyEnvContext.portal.shortcode,
        studyEnvContext.study.shortcode,
        studyEnvContext.currentEnv.environmentName,
        enrollee.shortcode,
        file.fileName
      )
      Store.addNotification(successNotification(`"${file.fileName}" deleted`))
      onDocumentDeleted()
    } catch (e) {
      Store.addNotification(failureNotification(`Failed to delete "${file.fileName}"`))
    }
  }

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
    { renderEmptyMessage(documents, 'No uploaded documents') }
  </>
}

const DownloadRecordList = ({ records }: { records: DownloadRecord[] }) => {
  if (records.length === 0) {
    return <span className='fst-italic text-muted'>none</span>
  }
  return (
    <ul className='ps-3 mb-0'>
      {records.map((record, i) => (
        <li key={i}>
          {instantToDefaultString(record.createdAt)}
          {' — '}
          {record.adminUserId ? 'Admin' : 'Participant'}
        </li>
      ))}
    </ul>
  )
}

const surveyResponseIdsToTaskNames = (
  enrollee: Enrollee, surveyResponseIds: string[]
) => {
  const associatedTasks = surveyResponseIds.map(surveyResponseId => {
    return enrollee.participantTasks.find(task => task.surveyResponseId === surveyResponseId)
  }).filter(task => task !== undefined)

  if (associatedTasks.length === 0) {
    return <div className={'fst-italic text-muted'}>none</div>
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
