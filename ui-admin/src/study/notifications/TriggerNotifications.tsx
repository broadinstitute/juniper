import React, { useState } from 'react'
import { currentIsoDate, instantToDefaultString } from '@juniper/ui-core'
import { StudyEnvContextT } from '../StudyEnvironmentRouter'
import { ColumnDef, getCoreRowModel, getSortedRowModel, SortingState, useReactTable } from '@tanstack/react-table'
import { useLoadingEffect } from '../../api/api-utils'
import Api, { Notification } from '../../api/api'
import { basicTableLayout, DownloadControl } from 'util/table/tableUtils'
import LoadingSpinner from 'util/LoadingSpinner'
import { useParams } from 'react-router-dom'
import InfoPopup from '../../components/forms/InfoPopup'
import { renderEmailActivityIcon } from '../participants/enrolleeView/EnrolleeTimeline'
import { enrolleeShortcodeColumn } from '../../util/table/columnUtils'

/** loads the list of notifications for a given trigger config */
export default function TriggerNotifications({ studyEnvContext }:
                                           { studyEnvContext: StudyEnvContextT }) {
  const { currentEnv, study, portal } = studyEnvContext
  const [tableData, setTableData] = useState<Notification[]>([])
  const [sorting, setSorting] = React.useState<SortingState>([{ 'id': 'createdAt', 'desc': true }])

  const triggerId = useParams().triggerId as string


  const columns: ColumnDef<Notification>[] = [
    enrolleeShortcodeColumn(studyEnvContext.currentEnvPath),
    {
      header: 'Sent To',
      accessorKey: 'sentTo'
    },
    {
      header: 'delivery type',
      accessorKey: 'deliveryType'
    },
    {
      header: 'delivery status',
      accessorKey: 'deliveryStatus'
    },
    {
      id: 'opened',
      header: () =>
        <div className={'d-flex align-items-center'}>
          <span>opened</span>
          <div onClick={e => e.stopPropagation()}><InfoPopup
            content={
              `Email activity may be unavailable due to a participant's email privacy
             settings. Additionally, emails sent more than 30 days ago may not have
             email activity associated with them.
             Note: please allow up to an hour for new activity to be reflected here.`
            }
          /></div>
        </div>,
      accessorKey: 'eventDetails.opensCount',
      meta: {
        columnType: 'boolean'
      },
      cell: ({ row }) => {
        return renderEmailActivityIcon(row.original)
      }
    },
    {
      header: 'time',
      accessorKey: 'createdAt',
      meta: {
        columnType: 'instant'
      },
      cell: info => instantToDefaultString(info.getValue() as number)
    }
  ]

  const { isLoading } = useLoadingEffect(async () => {
    const notifications = await Api.fetchTriggerNotifications(
      portal.shortcode,
      study.shortcode,
      currentEnv.environmentName,
      triggerId)
    setTableData(notifications)
  }, [triggerId])


  const table = useReactTable({
    data: tableData,
    columns,
    state: {
      sorting
    },
    onSortingChange: setSorting,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel()
  })

  return <div className={'w-100'}>
    <h5>Notifications</h5>
    {/* eslint-disable-next-line react/jsx-no-undef */}
    <LoadingSpinner isLoading={isLoading}>
      <div className="d-flex align-items-center">
        Sent {tableData.length} notifications
        <DownloadControl table={table}
          fileName={`SentNotifications-${currentIsoDate()}`}/>
      </div>

      {basicTableLayout(table)}
    </LoadingSpinner>
  </div>
}

