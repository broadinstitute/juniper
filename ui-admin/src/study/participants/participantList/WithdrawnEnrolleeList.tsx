import React, { useState } from 'react'
import { paramsFromContext, StudyEnvContextT } from '../../StudyEnvironmentRouter'
import { renderPageHeader } from '../../../util/pageUtils'
import { useLoadingEffect } from '../../../api/api-utils'
import Api, { WithdrawnEnrollee } from '../../../api/api'
import { basicTableLayout } from '../../../util/tableUtils'
import LoadingSpinner from '../../../util/LoadingSpinner'
import { ColumnDef, getCoreRowModel, getSortedRowModel, SortingState, useReactTable } from '@tanstack/react-table'
import { instantToDefaultString } from '@juniper/ui-core'
import { NavBreadcrumb } from '../../../navbar/AdminNavbar'
import { DocsKey, ZendeskLink } from '../../../util/zendeskUtils'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faInfoCircle } from '@fortawesome/free-solid-svg-icons'

type WithdrawnEnrolleeExtract = WithdrawnEnrollee & {
  userDataObj: { username: string, createdAt: number }
}

/**
 * show a list of withdrawn enrollees with account information
 */
export default function WithdrawnEnrolleeList({ studyEnvContext }: { studyEnvContext: StudyEnvContextT}) {
  const [enrollees, setEnrollees] = useState<WithdrawnEnrolleeExtract[]>([])
  const [sorting, setSorting] = React.useState<SortingState>([{ 'id': 'createdAt', 'desc': true }])

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
    <NavBreadcrumb value={'withdrawnList'}>Withdrawn</NavBreadcrumb>
    <FontAwesomeIcon icon={faInfoCircle}/> More information about the
    <ZendeskLink doc={DocsKey.WITHDRAWAL}> withdrawl process</ZendeskLink>.
    <LoadingSpinner isLoading={isLoading}>
      <div className="d-flex align-items-center justify-content-between">
        { basicTableLayout(table) }
      </div>
    </LoadingSpinner>
  </div>
}
