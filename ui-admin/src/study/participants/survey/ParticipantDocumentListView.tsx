import React from 'react'
import {
  ColumnDef,
  getCoreRowModel,
  useReactTable
} from '@tanstack/react-table'
import {
  Enrollee,
  ParticipantFile, saveBlobAsDownload
} from '@juniper/ui-core'
import { basicTableLayout, renderEmptyMessage } from 'util/table/tableUtils'
import { createdAtColumn } from 'util/table/tableColumnUtils'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faDownload } from '@fortawesome/free-solid-svg-icons'
import Api from 'api/api'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'

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
      accessorKey: 'surveyResponseIds'
    }] : []),
    {
      header: 'File Type',
      accessorKey: 'fileType'
    },
    {
      header: 'Actions',
      cell: ({ row }) => {
        return <button className='btn btn-secondary' onClick={() => download(row.original)}>
          <FontAwesomeIcon icon={faDownload}/>
        </button>
      }
    }
  ]

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
