import React, { useState } from 'react'
import Api, { ExportIntegration, ExportIntegrationJob } from 'api/api'
import LoadingSpinner from 'util/LoadingSpinner'
import {
  ColumnDef,
  getCoreRowModel,
  getSortedRowModel,
  SortingState,
  useReactTable
} from '@tanstack/react-table'
import {
  basicTableLayout,
  renderEmptyMessage
} from 'util/tableUtils'
import { instantToDefaultString } from '@juniper/ui-core'
import { useLoadingEffect } from 'api/api-utils'
import { renderPageHeader } from 'util/pageUtils'
import {
  paramsFromContext,
  StudyEnvContextT,
  studyEnvExportIntegrationPath
} from '../../StudyEnvironmentRouter'
import { Link } from 'react-router-dom'
import { useAdminUserContext } from 'providers/AdminUserProvider'
import { AdminUser } from 'api/adminUser'

type ExportIntegrationJobMatched = ExportIntegrationJob & {
  integration: ExportIntegration
  adminUser?: AdminUser
}

/** show the list of export integrations for the environment */
export default function ExportIntegrationJobList({ studyEnvContext }:
  {studyEnvContext: StudyEnvContextT }) {
  const [integrations, setIntegrations] = useState<ExportIntegration[]>([])
  const [integrationJobs, setIntegrationJobs] = useState<ExportIntegrationJobMatched[]>([])
  const { users } = useAdminUserContext()
  const [sorting, setSorting] = React.useState<SortingState>([{ 'id': 'startedAt', 'desc': true }])

  const columns: ColumnDef<ExportIntegrationJobMatched>[] = [{
    header: 'Name',
    accessorKey: 'integration.name',
    cell: info => <Link to={studyEnvExportIntegrationPath(paramsFromContext(studyEnvContext),
      info.row.original.exportIntegrationId)}>{info.getValue() as string}</Link>
  }, {
    header: 'Destination',
    accessorKey: 'integration.destinationType'
  }, {
    header: 'Started',
    accessorKey: 'startedAt',
    meta: {
      columnType: 'instant'
    },
    cell: info => instantToDefaultString(info.getValue() as number)
  }, {
    header: 'Operator',
    id: 'operator',
    cell: info => info.row.original.adminUser ? info.row.original.adminUser.username :
      info.row.original.systemProcess
  }, {
    header: 'Status',
    accessorKey: 'status'
  }]

  const table = useReactTable({
    data: integrationJobs,
    columns,
    state: {
      sorting
    },
    onSortingChange: setSorting,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel()
  })

  const { isLoading } = useLoadingEffect(async () => {
    const [fetchedIntegrations, fetchedJobs] = await Promise.all([
      Api.fetchExportIntegrations(paramsFromContext(studyEnvContext)),
      Api.fetchExportIntegrationJobs(paramsFromContext(studyEnvContext))
    ])

    setIntegrations(fetchedIntegrations)
    setIntegrationJobs(fetchedJobs.map(job => ({
      ...job,
      integration: fetchedIntegrations.find(integration => integration.id === job.exportIntegrationId)!,
      adminUser: users.find(user => user.id === job.creatingAdminUserId)
    })))
  }, [studyEnvContext.currentEnvPath, users.length])

  return <div className="container-fluid px-4 py-2">
    { renderPageHeader('Export Integration Job History') }
    <LoadingSpinner isLoading={isLoading}>
      { basicTableLayout(table) }
      { renderEmptyMessage(integrations, 'No jobs') }
    </LoadingSpinner>
  </div>
}
