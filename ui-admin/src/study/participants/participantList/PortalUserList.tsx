import React, { useMemo, useState } from 'react'
import { useLoadingEffect } from 'api/api-utils'
import Api, { Study } from 'api/api'
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
import { Enrollee, instantToDefaultString, ParticipantUser, instantToDateString } from '@juniper/ui-core'
import { NavBreadcrumb } from 'navbar/AdminNavbar'
import { studyEnvPath } from '../../StudyEnvironmentRouter'
import { Link } from 'react-router-dom'

type ParticipantUserWithEnrollees = ParticipantUser & {
  enrollees: Enrollee[]
}

/**
 * show a list of withdrawn enrollees with account information
 */
export default function PortalUserList({ portalShortcode, envName }:
{ portalShortcode: string, envName: string}) {
  const [users, setUsers] = useState<ParticipantUserWithEnrollees[]>([])
  const [sorting, setSorting] = React.useState<SortingState>([{ 'id': 'createdAt', 'desc': true }])
  const [columnVisibility, setColumnVisibility] = React.useState<VisibilityState>({
    'email': false
  })
  const [envMap, setEnvMap] = useState<Record<string, Study>>({})

  const { isLoading } = useLoadingEffect(async () => {
    const studies = await Api.fetchStudiesWithEnvs(portalShortcode, envName)
    setEnvMap(studies.reduce((acc, study) => {
      (acc as Record<string, Study>)[study.studyEnvironments[0].id] = study
      return acc
    }, {}))
    const result = await Api.fetchParticipantUsers(portalShortcode, envName)
    const mappedResult = result.participantUsers.map(user => ({
      ...user,
      enrollees: result.enrollees.filter(enrollee => enrollee.participantUserId === user.id)
    }))
    setUsers(mappedResult)
  }, [portalShortcode, envName])

  const columns: ColumnDef<ParticipantUserWithEnrollees>[] = useMemo(() => {
    const cols: ColumnDef<ParticipantUserWithEnrollees>[] = [{
      header: 'user',
      accessorKey: 'username'
    }, {
      header: 'created',
      accessorKey: 'createdAt',
      meta: {
        columnType: 'instant'
      },
      cell: info => instantToDefaultString(info.getValue() as number)
    }, {
      header: 'lastLogin',
      accessorKey: 'lastLogin',
      meta: {
        columnType: 'instant'
      },
      cell: info => instantToDefaultString(info.getValue() as number)
    }]
    Object.keys(envMap).forEach(envId => {
      cols.push({
        header: envMap[envId].shortcode,
        id: envMap[envId].shortcode,
        cell: info => {
          const enrollee = info.row.original.enrollees.find(enrollee => enrollee.studyEnvironmentId === envId)
          return enrollee ? <span><Link to={`${studyEnvPath(portalShortcode,
            envMap[envId].shortcode, envName)}/participants/${enrollee.shortcode}`}>
            {enrollee.shortcode}
          </Link> <span className="text-muted fst-italic">({instantToDateString(enrollee.createdAt)})</span>
          </span> : ''
        }
      })
    })
    return cols
  }, [portalShortcode, envName, Object.keys(envMap).length])

  const table = useReactTable({
    data: users,
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
    <NavBreadcrumb value={'participantUserList'}>Accounts</NavBreadcrumb>
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
