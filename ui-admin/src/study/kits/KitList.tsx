import React, { useEffect, useState } from 'react'
import _fromPairs from 'lodash/fromPairs'
import _groupBy from 'lodash/groupBy'
import _keys from 'lodash/keys'
import { Tab, Tabs } from 'react-bootstrap'
import { Link } from 'react-router-dom'
import {
  ColumnDef,
  getCoreRowModel, getFilteredRowModel, getSortedRowModel, SortingState, useReactTable, VisibilityState
} from '@tanstack/react-table'

import Api, { KitRequest } from 'api/api'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import LoadingSpinner from 'util/LoadingSpinner'
import { basicTableLayout, ColumnVisibilityControl } from 'util/tableUtils'
import { instantToDateString, isoToInstant } from 'util/timeUtils'

type KitStatusTabConfig = {
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
  'pepperStatus_receiveDate': false
}

const statusTabs: Record<string, KitStatusTabConfig> = {
  'CREATED': {
    label: 'Created',
    additionalColumns: []
  },
  'LABELED': {
    label: 'Labeled',
    additionalColumns: [
      'pepperStatus_labelDate', 'pepperStatus_trackingNumber'
    ]
  },
  'SCANNED': {
    label: 'Sent',
    additionalColumns: [
      'pepperStatus_labelDate', 'pepperStatus_trackingNumber',
      'pepperStatus_scanDate', 'pepperStatus_returnTrackingNumber'
    ]
  },
  'RECEIVED': {
    label: 'Returned',
    additionalColumns: [
      'pepperStatus_labelDate', 'pepperStatus_trackingNumber',
      'pepperStatus_scanDate', 'pepperStatus_returnTrackingNumber',
      'pepperStatus_receiveDate'
    ]
  }
}

const initialColumnVisibility = (status: string): VisibilityState => {
  return {
    ...defaultColumns,
    ..._fromPairs(statusTabs[status].additionalColumns?.map(c => { return [c, true] }))
  }
}

const pepperStatusToHumanStatus = (dsmStatus?: string): string => {
  return dsmStatus
    ? (statusTabs[dsmStatus]?.label || (`(${dsmStatus})`))
    : '(unknown)'
}

/** Loads sample kits for a study and shows them as a list. */
export default function KitList({ studyEnvContext }: { studyEnvContext: StudyEnvContextT }) {
  const { portal, study, currentEnv } = studyEnvContext
  const [isLoading, setIsLoading] = useState(true)
  const [activeTab, setActiveTab] = useState<string | null>(_keys(statusTabs)[0])
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

  const kitsByStatus = _groupBy(kits, kit => kit.pepperStatus?.currentStatus || '')

  return <LoadingSpinner isLoading={isLoading}>
    <Tabs
      activeKey={activeTab ?? undefined}
      mountOnEnter
      unmountOnExit
      onSelect={setActiveTab}
    >
      { _keys(statusTabs).map(status => {
        return <Tab
          key={status}
          title={`${kitsByStatus[status]?.length || 0} ${pepperStatusToHumanStatus(status)}`}
          eventKey={status}>
          <KitListView studyEnvContext={studyEnvContext} kits={kitsByStatus[status] || []} status={status}/>
        </Tab>
      })}
    </Tabs>
  </LoadingSpinner>
}

/** Renders a table with a list of kits. */
function KitListView({ studyEnvContext, kits, status }: {
  studyEnvContext: StudyEnvContextT,
  kits: KitRequest[],
  status: string
}) {
  const { currentEnvPath } = studyEnvContext
  const [columnVisibility, setColumnVisibility] = useState<VisibilityState>(initialColumnVisibility(status))
  const [sorting, setSorting] = React.useState<SortingState>([])

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
