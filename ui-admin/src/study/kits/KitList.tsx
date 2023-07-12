import React, { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import {
  ColumnDef,
  getCoreRowModel, getFilteredRowModel, getSortedRowModel, useReactTable
} from '@tanstack/react-table'

import Api, { KitRequest } from 'api/api'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import LoadingSpinner from 'util/LoadingSpinner'
import { basicTableLayout } from 'util/tableUtils'
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

/**
 * Shows a list of all kits for a study.
 */
export default function KitList({ studyEnvContext }: { studyEnvContext: StudyEnvContextT }) {
  const { portal, study, currentEnv, currentEnvPath } = studyEnvContext
  const [isLoading, setIsLoading] = useState(true)
  const [kits, setKits] = useState<KitRequest[]>([])

  const loadKits = async () => {
    setIsLoading(true)
    const kits = await Api.fetchKitsForKitManagement(
      portal.shortcode, study.shortcode, currentEnv.environmentName)
    setKits(kits)
    setIsLoading(false)
  }

  useEffect(() => {
    loadKits()
  }, [])

  // Inspired by https://stackoverflow.com/a/70171464/244191
  type ApplyFunction<X, T, R> = (all: X, current: T) => R
  type GetFunction<T, R> = (t: T) => R

  const groupAndApplyByIndex = <T, V extends string | number, R>(data: Array<T>,
    get: GetFunction<T, V>,
    apply: ApplyFunction<Record<V, R>, V, R>) => {
    return data.reduce((all, element) => {
      return {
        ...all,
        [get(element)]: apply(all, get(element))
      }
    }, {} as Record<V, R>)
  }

  const getStatus: GetFunction<KitRequest, string> = k => k.pepperStatus?.currentStatus || ''
  const countsByStatus = groupAndApplyByIndex(kits, getStatus,
    (all, element) => ((all[element] as number || 0) + 1))

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
      filterOptions: [
        { value: true, label: 'Created' },
        { value: true, label: 'Prepared' },
        { value: true, label: 'Sent' },
        { value: true, label: 'Received' }
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
    <LoadingSpinner isLoading={isLoading}>
      <div>
        <span>Total Kits: {kits.length}</span>
        <span className='ms-3'><>{pepperStatusToHumanStatus('CREATED')}: {countsByStatus['CREATED'] || 0}</></span>
        <span className='ms-3'><>{pepperStatusToHumanStatus('LABELED')}: {countsByStatus['LABELED'] || 0}</></span>
        <span className='ms-3'><>{pepperStatusToHumanStatus('SCANNED')}: {countsByStatus['SCANNED'] || 0}</></span>
        <span className='ms-3'><>{pepperStatusToHumanStatus('RECEIVED')}: {countsByStatus['RECEIVED'] || 0}</></span>
      </div>
      {basicTableLayout(table, { filterable: true })}
    </LoadingSpinner>
  </>
}
