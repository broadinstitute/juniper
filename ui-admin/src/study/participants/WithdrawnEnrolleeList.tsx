import React, { useState } from 'react'
import { paramsFromContext, StudyEnvContextT } from '../StudyEnvironmentRouter'
import { renderPageHeader } from 'util/pageUtils'
import { useLoadingEffect } from 'api/api-utils'
import Api, { WithdrawnEnrollee } from 'api/api'
import { basicTableLayout } from 'util/tableUtils'
import LoadingSpinner from 'util/LoadingSpinner'
import { ColumnDef, getCoreRowModel, getSortedRowModel, SortingState, useReactTable } from '@tanstack/react-table'
import { instantToDefaultString } from '@juniper/ui-core'



/**
 * show a list of withdrawn enrollees with account information
 */
export default function WithdrawnEnrolleeList({ studyEnvContext }: { studyEnvContext: StudyEnvContextT}) {
  const [enrollees, setEnrollees] = useState<WithdrawnEnrollee[]>([])
  const [sorting, setSorting] = React.useState<SortingState>([{ 'id': 'createdAt', 'desc': true }])

  const { isLoading } = useLoadingEffect(async () => {
    const result = await Api.fetchWithdrawnEnrollees(paramsFromContext(studyEnvContext))
    setEnrollees(result)
  }, [studyEnvContext.currentEnvPath])

  const columns: ColumnDef<WithdrawnEnrollee>[] = [{
    header: 'Shortcode',
    accessorKey: 'shortcode'
  }, {
    header: 'Email',
    accessorKey: 'Email'
  }, {
    header: 'Withdrawn',
    accessorKey: 'createdAt',
    meta: {
      columnType: 'instant'
    },
    cell: info => instantToDefaultString(info.getValue() as number)
  }]

  const table = useReactTable({
    data: enrollees,
    columns,
    state: {
      sorting
    },
    enableRowSelection: true,
    onSortingChange: setSorting,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel()
  })
  return <div className="container-fluid px-4 py-2">
    { renderPageHeader('Withdrawn enrollees') }
    <LoadingSpinner isLoading={isLoading}>
      <div className="d-flex align-items-center justify-content-between">
        { basicTableLayout(table) }
      </div>
    </LoadingSpinner>
  </div>
}
