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
import { StudyEnvContextT, studyEnvPath } from '../../StudyEnvironmentRouter'
import { Link } from 'react-router-dom'
import { renderPageHeader } from '../../../util/pageUtils'
import { ParticipantListViewSwitcher } from './ParticipantListViewSwitcher'
import _uniq from 'lodash/uniq'

type ParticipantUserWithEnrollees = ParticipantUser & {
  enrollees: Enrollee[]
}

/**
 * show a list of withdrawn enrollees with account information
 */
export default function PortalUserList({ studyEnvContext }:
{ studyEnvContext: StudyEnvContextT}) {
  const [users, setUsers] = useState<ParticipantUserWithEnrollees[]>([])
  const [sorting, setSorting] = React.useState<SortingState>([{ 'id': 'createdAt', 'desc': true }])
  const [columnVisibility, setColumnVisibility] = React.useState<VisibilityState>({
    'email': false
  })
  const [envMap, setEnvMap] = useState<Record<string, Study>>({})

  const { isLoading } = useLoadingEffect(async () => {
    const studies = await Api.fetchStudiesWithEnvs(studyEnvContext.portal.shortcode,
      studyEnvContext.currentEnv.environmentName)
    setEnvMap(studies.reduce((acc, study) => {
      (acc as Record<string, Study>)[study.studyEnvironments[0].id] = study
      return acc
    }, {}))
    const result = await Api.fetchParticipantUsers(studyEnvContext.portal.shortcode,
      studyEnvContext.currentEnv.environmentName)
    const mappedResult = result.participantUsers.map(user => ({
      ...user,
      enrollees: result.enrollees.filter(enrollee => enrollee.participantUserId === user.id)
    }))
    setUsers(mappedResult)
  }, [studyEnvContext.portal.shortcode, studyEnvContext.currentEnv.environmentName])

  const columns: ColumnDef<ParticipantUserWithEnrollees>[] = useMemo(() => {
    const cols: ColumnDef<ParticipantUserWithEnrollees>[] = [{
      header: 'User',
      accessorKey: 'username'
    }, {
      header: 'Joined',
      accessorKey: 'createdAt',
      meta: {
        columnType: 'instant'
      },
      cell: info => instantToDefaultString(info.getValue() as number)
    }, {
      header: 'Last Login',
      accessorKey: 'lastLogin',
      meta: {
        columnType: 'instant'
      },
      cell: info => instantToDefaultString(info.getValue() as number)
    }, {
      header: 'Family name',
      id: 'familyName',
      cell: info =>
        _uniq(info.row.original.enrollees.map(enrollee => enrollee.profile.familyName)).join(', ')
    }, {
      header: 'Given name',
      id: 'givenName',
      cell: info =>
        _uniq(info.row.original.enrollees.map(enrollee => enrollee.profile.givenName)).join(', ')
    }]
    Object.keys(envMap).forEach(envId => {
      cols.push({
        header: envMap[envId].shortcode,
        id: envMap[envId].shortcode,
        cell: info => {
          const enrollee = info.row.original.enrollees.find(enrollee => enrollee.studyEnvironmentId === envId)
          return enrollee ? <span><Link to={`${studyEnvPath(studyEnvContext.portal.shortcode,
            envMap[envId].shortcode, studyEnvContext.currentEnv.environmentName)}/participants/${enrollee.shortcode}`}>
            {enrollee.shortcode}
          </Link> <span className="text-muted fst-italic">({instantToDateString(enrollee.createdAt)})</span>
          </span> : ''
        }
      })
    })
    return cols
  }, [studyEnvContext.portal.shortcode, studyEnvContext.currentEnv.environmentName, Object.keys(envMap).length])

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
  return <div className="container-fluid px-4 py-2">
    <NavBreadcrumb value={'participantUserList'}>Accounts</NavBreadcrumb>
    <div className="d-flex align-items-center justify-content-between ">
      {renderPageHeader('Account List')}
      <ParticipantListViewSwitcher
        studyEnvConfig={studyEnvContext.currentEnv.studyEnvironmentConfig}
      />
    </div>
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
