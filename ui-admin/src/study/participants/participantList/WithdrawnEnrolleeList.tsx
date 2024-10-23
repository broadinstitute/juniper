import React, { useState } from 'react'
import { paramsFromContext, StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import { useLoadingEffect } from 'api/api-utils'
import Api, { WithdrawnEnrollee } from 'api/api'
import { basicTableLayout, ColumnVisibilityControl } from 'util/tableUtils'
import LoadingSpinner from 'util/LoadingSpinner'
import {
  ColumnDef,
  getCoreRowModel,
  getSortedRowModel,
  SortingState,
  useReactTable,
  VisibilityState
} from '@tanstack/react-table'
import { instantToDefaultString } from '@juniper/ui-core'
import { NavBreadcrumb } from 'navbar/AdminNavbar'
import { DocsKey, ZendeskLink } from 'util/zendeskUtils'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faInfoCircle } from '@fortawesome/free-solid-svg-icons'
import { renderPageHeader } from '../../../util/pageUtils'
import { ParticipantListViewSwitcher } from './ParticipantListViewSwitcher'

type WithdrawnEnrolleeExtract = WithdrawnEnrollee & {
  userDataObj: { username: string, createdAt: number }
}

/**
 * show a list of withdrawn enrollees with account information
 */
export default function WithdrawnEnrolleeList({ studyEnvContext }: { studyEnvContext: StudyEnvContextT}) {
  const [enrollees, setEnrollees] = useState<WithdrawnEnrolleeExtract[]>([])
  const [sorting, setSorting] = React.useState<SortingState>([{ 'id': 'createdAt', 'desc': true }])
  const [columnVisibility, setColumnVisibility] = React.useState<VisibilityState>({
    'email': false
  })
  const { isLoading } = useLoadingEffect(async () => {
    const result = await Api.fetchWithdrawnEnrollees(paramsFromContext(studyEnvContext))
    const extracts: WithdrawnEnrolleeExtract[] = result.map(enrollee => ({
      ...enrollee,
      userDataObj: JSON.parse(enrollee.userData)
    }))
    setEnrollees(extracts)
  }, [studyEnvContext.currentEnvPath])

  const columns: ColumnDef<WithdrawnEnrolleeExtract>[] = [{
    header: 'Shortcode',
    accessorKey: 'shortcode'
  }, {
    header: 'Email',
    id: 'email',
    accessorKey: 'userDataObj.username'
  }, {
    header: 'Account created',
    accessorKey: 'userDataObj.createdAt',
    meta: {
      columnType: 'instant'
    },
    cell: info => instantToDefaultString(info.getValue() as number)
  }, {
    header: 'Withdrawn',
    accessorKey: 'createdAt',
    meta: {
      columnType: 'instant'
    },
    cell: info => instantToDefaultString(info.getValue() as number)
  }, {
    header: 'Reason',
    accessorKey: 'reason'
  }, {
    header: 'Note',
    accessorKey: 'note'
  }]

  const table = useReactTable({
    data: enrollees,
    columns,
    state: {
      sorting,
      columnVisibility
    },
    onColumnVisibilityChange: setColumnVisibility,
    enableRowSelection: true,
    onSortingChange: setSorting,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel()
  })
  return <div className="container-fluid px-4 pt-4">
    <div className="d-flex align-items-center justify-content-between ">
      {renderPageHeader('Withdrawn Enrollees')}
      <ParticipantListViewSwitcher
        studyEnvConfig={studyEnvContext.currentEnv.studyEnvironmentConfig}
      />
    </div>
    <NavBreadcrumb value={'withdrawnList'}>Withdrawn</NavBreadcrumb>
    <FontAwesomeIcon icon={faInfoCircle}/> More information about the
    <ZendeskLink doc={DocsKey.WITHDRAWAL}> withdrawal process</ZendeskLink>.
    <LoadingSpinner isLoading={isLoading}>
      <div className="d-flex justify-content-end">
        <ColumnVisibilityControl table={table}/>
      </div>
      <div className="d-flex align-items-center justify-content-between">
        { basicTableLayout(table) }
      </div>
    </LoadingSpinner>
  </div>
}
