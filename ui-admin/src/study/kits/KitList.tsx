import React, { useEffect, useState } from 'react'
import _fromPairs from 'lodash/fromPairs'
import _groupBy from 'lodash/groupBy'
import { Link, Navigate, NavLink, Route, Routes } from 'react-router-dom'
import {
  ColumnDef,
  getCoreRowModel, getFilteredRowModel, getSortedRowModel, SortingState, useReactTable, VisibilityState
} from '@tanstack/react-table'

import Api, { KitRequest, PepperKitStatus } from 'api/api'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import LoadingSpinner from 'util/LoadingSpinner'
import { basicTableLayout, ColumnVisibilityControl } from 'util/tableUtils'
import { instantToDateString, isoToInstant } from 'util/timeUtils'

type KitStatusTabConfig = {
  status: string,
  label: string,
  additionalColumns?: string[]
}

const defaultColumns = {
  'enrollee_shortcode': true,
  'kitType_displayName': true,
  'createdAt': true,
  'pepperStatus_labelDate': false,
  'pepperStatus_trackingNumber': false,
  'pepperStatus_scanDate': false,
  'pepperStatus_returnTrackingNumber': false,
  'pepperStatus_receiveDate': false,
  'pepperStatus': false
}

const statusTabs: KitStatusTabConfig[] = [
  {
    status: 'CREATED',
    label: 'Created',
    additionalColumns: []
  },
  {
    status: 'LABELED',
    label: 'Labeled',
    additionalColumns: [
      'pepperStatus_labelDate', 'pepperStatus_trackingNumber'
    ]
  },
  {
    status: 'SCANNED',
    label: 'Sent',
    additionalColumns: [
      'pepperStatus_labelDate', 'pepperStatus_trackingNumber',
      'pepperStatus_scanDate', 'pepperStatus_returnTrackingNumber'
    ]
  },
  {
    status: 'RECEIVED',
    label: 'Returned',
    additionalColumns: [
      'pepperStatus_labelDate', 'pepperStatus_trackingNumber',
      'pepperStatus_scanDate', 'pepperStatus_returnTrackingNumber',
      'pepperStatus_receiveDate'
    ]
  },
  {
    status: 'ERROR',
    label: 'Issues',
    additionalColumns: [
      'pepperStatus_labelDate', 'pepperStatus_trackingNumber',
      'pepperStatus_scanDate', 'pepperStatus_returnTrackingNumber',
      'pepperStatus_receiveDate',
      'pepperStatus'
    ]
  }
]

const initialColumnVisibility = (tab: KitStatusTabConfig): VisibilityState => {
  return {
    ...defaultColumns,
    ..._fromPairs(tab.additionalColumns?.map(c => { return [c, true] }))
  }
}

const pepperStatusToHumanStatus = (pepperStatus?: PepperKitStatus): string => {
  const tab = statusTabs.find(tab => tab.status === pepperStatus?.currentStatus)
  return tab?.label || pepperStatus?.currentStatus || '(unknown)'
}

/** Loads sample kits for a study and shows them as a list. */
export default function KitList({ studyEnvContext }: { studyEnvContext: StudyEnvContextT }) {
  const { portal, study, currentEnv } = studyEnvContext
  const [isLoading, setIsLoading] = useState(true)
  const [activeTab, setActiveTab] = useState<string | null>(statusTabs[0].status)
  const [kits, setKits] = useState<KitRequest[]>([])

  const loadKits = async () => {
    setIsLoading(true)
    const kits = await Api.fetchKitsByStudyEnvironment(
      portal.shortcode, study.shortcode, currentEnv.environmentName)
    setKits(kits)
    setIsLoading(false)
  }

  useEffect(() => {
    loadKits()
  }, [studyEnvContext.study.shortcode, studyEnvContext.currentEnv.environmentName])

  const kitsByStatus = _groupBy(kits, kit => {
    return statusTabs.find(tab => tab.status === kit.pepperStatus?.currentStatus)?.status || 'ERROR'
  })

  const tabLinkStyle = ({ isActive }: {isActive: boolean}) => ({
    borderBottom: isActive ? '3px solid #333': '',
    background: isActive ? '#ddd' : ''
  })

  return <LoadingSpinner isLoading={isLoading}>
    <div className="container">
      <div className="d-flex w-100" style={{ backgroundColor: '#ccc' }}>
        { statusTabs.map(tab => {
          const kits = kitsByStatus[tab.status] || []
          return <NavLink key={tab.status} to={tab.status} style={tabLinkStyle}>
            <div className="py-3 px-5">
              {kits?.length} {tab.label}
            </div>
          </NavLink>
        })}
      </div>
      <Routes>
        <Route index element={<Navigate to='CREATED' replace={true}/>}/>
        { statusTabs.map(tab => {
          return <Route key={tab.status} path={tab.status} element={
            <KitListView
              studyEnvContext={studyEnvContext}
              tab={tab.status}
              kits={kitsByStatus[tab.status] || []}
              initialColumnVisibility={initialColumnVisibility(tab)}/>
          }/>
        })}
      </Routes>
    </div>
  </LoadingSpinner>
}

/** Renders a table with a list of kits. */
function KitListView({ studyEnvContext, tab, kits, initialColumnVisibility }: {
  studyEnvContext: StudyEnvContextT,
  tab: string
  kits: KitRequest[],
  initialColumnVisibility: VisibilityState
}) {
  const { currentEnvPath } = studyEnvContext
  const [currentTab, setCurrentTab] = useState(tab)
  const [columnVisibility, setColumnVisibility] = useState<VisibilityState>(initialColumnVisibility)
  const [sorting, setSorting] = useState<SortingState>([])

  console.log('initialColumnVisibility', initialColumnVisibility)

  if (tab !== currentTab) {
    setColumnVisibility(initialColumnVisibility)
    setCurrentTab(tab)
  }

  const columns: ColumnDef<KitRequest, string>[] = [{
    header: 'Enrollee shortcode',
    accessorKey: 'enrollee.shortcode',
    meta: {
      columnType: 'string'
    },
    cell: data => <Link to={`${currentEnvPath}/participants/${data.getValue()}`}>{data.getValue()}</Link>,
    enableColumnFilter: false
  }, {
    header: 'Kit type',
    accessorKey: 'kitType.displayName',
    enableColumnFilter: false
  }, {
    header: 'Created',
    accessorKey: 'createdAt',
    cell: data => instantToDateString(Number(data.getValue())),
    enableColumnFilter: false
  }, {
    header: 'Labeled',
    accessorKey: 'pepperStatus.labelDate',
    cell: data => instantToDateString(isoToInstant(data.getValue())),
    enableColumnFilter: false
  }, {
    header: 'Tracking Number',
    accessorKey: 'pepperStatus.trackingNumber',
    enableColumnFilter: false
  }, {
    header: 'Sent',
    accessorKey: 'pepperStatus.scanDate',
    cell: data => instantToDateString(isoToInstant(data.getValue())),
    enableColumnFilter: false
  }, {
    header: 'Return Tracking Number',
    accessorKey: 'pepperStatus.returnTrackingNumber',
    enableColumnFilter: false
  }, {
    header: 'Returned',
    accessorKey: 'pepperStatus.receiveDate',
    cell: data => instantToDateString(isoToInstant(data.getValue())),
    enableColumnFilter: false
  }, {
    header: 'Status',
    accessorKey: 'pepperStatus',
    cell: data => pepperStatusToHumanStatus(data.row.original.pepperStatus),
    enableColumnFilter: false
  }]

  const table = useReactTable({
    data: kits,
    columns,
    state: { columnVisibility, sorting },
    onColumnVisibilityChange: setColumnVisibility,
    onSortingChange: setSorting,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel(),
    getFilteredRowModel: getFilteredRowModel()
  })

  return <>
    <div className="d-flex align-items-center justify-content-between">
      <ColumnVisibilityControl table={table}/>
    </div>
    {basicTableLayout(table, { filterable: true })}
  </>
}
