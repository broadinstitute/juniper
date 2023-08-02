import React, { useEffect, useState } from 'react'
import _countBy from 'lodash/countBy'
import { Link } from 'react-router-dom'
import {
  ColumnDef,
  getCoreRowModel, getFilteredRowModel, getSortedRowModel, useReactTable
} from '@tanstack/react-table'

import Api, { KitRequest } from 'api/api'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import LoadingSpinner from 'util/LoadingSpinner'
import {basicTableLayout, FilterType} from 'util/tableUtils'
import { instantToDateString, isoToInstant } from 'util/timeUtils'

const pepperStatusToHumanStatus = (dsmStatus?: string): string => {
  const statusMap: Record<string, string> = {
    'CREATED': 'Created',
    'LABELED': 'Prepared',
    'SCANNED': 'Sent',
    'RECEIVED': 'Returned'
  }
  return dsmStatus
    ? (statusMap[dsmStatus] || (`(${  dsmStatus  })`))
    : '(unknown)'
}

/** Loads sample kits for a study and shows them as a list. */
export default function KitList({ studyEnvContext }: { studyEnvContext: StudyEnvContextT }) {
  const { portal, study, currentEnv } = studyEnvContext
  const [isLoading, setIsLoading] = useState(true)
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
  }, [])

  return <LoadingSpinner isLoading={isLoading}>
    <KitListView kits={kits} studyEnvContext={studyEnvContext}/>
  </LoadingSpinner>
}

/** Renders a table with a list of kits. */
export function KitListView({ studyEnvContext, kits }: { studyEnvContext: StudyEnvContextT, kits: KitRequest[] }) {
  const { currentEnvPath } = studyEnvContext

  const countsByStatus = _countBy(kits, kit => kit.pepperStatus?.currentStatus || '')

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
    header: 'Prepared',
    accessorKey: 'pepperStatus.labelDate',
    cell: data => instantToDateString(isoToInstant(data.getValue())),
    enableColumnFilter: false
  }, {
    header: 'Sent',
    accessorKey: 'pepperStatus.scanDate',
    cell: data => instantToDateString(isoToInstant(data.getValue())),
    enableColumnFilter: false
  }, {
    header: 'Returned',
    accessorKey: 'pepperStatus.receiveDate',
    cell: data => instantToDateString(isoToInstant(data.getValue())),
    enableColumnFilter: false
  }, {
    header: 'Status',
    accessorKey: 'pepperStatus.currentStatus',
    accessorFn: data => pepperStatusToHumanStatus(data?.pepperStatus?.currentStatus),
    meta: {
      columnType: 'string',
      filterType: FilterType.Select,
      filterOptions: [
        { value: 'created', label: 'Created' },
        { value: 'prepared', label: 'Prepared' },
        { value: 'sent', label: 'Sent' },
        { value: 'returned', label: 'Returned' }
      ]
    },
    filterFn: 'equalsString'
  }]

  const table = useReactTable({
    data: kits || [],
    columns,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel(),
    getFilteredRowModel: getFilteredRowModel()
  })

  return <>
    <div>
      <span>Total Kits: {kits.length}</span>
      <span className='ms-3'><>{pepperStatusToHumanStatus('CREATED')}: {countsByStatus['CREATED'] || 0}</></span>
      <span className='ms-3'><>{pepperStatusToHumanStatus('LABELED')}: {countsByStatus['LABELED'] || 0}</></span>
      <span className='ms-3'><>{pepperStatusToHumanStatus('SCANNED')}: {countsByStatus['SCANNED'] || 0}</></span>
      <span className='ms-3'><>{pepperStatusToHumanStatus('RECEIVED')}: {countsByStatus['RECEIVED'] || 0}</></span>
    </div>
    {basicTableLayout(table, { filterable: true })}
  </>
}
