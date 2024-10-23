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
import { Link, Navigate, NavLink, Route, Routes } from 'react-router-dom'
import { renderPageHeader } from 'util/pageUtils'
import _uniq from 'lodash/uniq'
import { tabLinkStyle } from 'util/subNavStyles'
import ParticipantMergeView from '../merge/ParticipantMergeView'
import useParticipantDupeTab from '../merge/UseParticipantDupeTab'
import { ParticipantListViewSwitcher } from './ParticipantListViewSwitcher'

export type ParticipantUserWithEnrollees = ParticipantUser & {
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

  const { isLoading, reload } = useLoadingEffect(async () => {
    // first get all the studyenvironments for this portal -- that will be needed to determine column headers
    const studies = await Api.fetchStudiesWithEnvs(studyEnvContext.portal.shortcode,
      studyEnvContext.currentEnv.environmentName)
    // map the studies by their studyEnvironmentId
    setEnvMap(studies.reduce((acc, study) => {
      (acc as Record<string, Study>)[study.studyEnvironments[0].id] = study
      return acc
    }, {}))
    const result = await Api.fetchParticipantUsers(studyEnvContext.portal.shortcode,
      studyEnvContext.currentEnv.environmentName)

    // convert the independent lists of users and enrollees into a single list of users with enrollees
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

  const tabs = [
    {
      name: 'All', path: 'all', component: <div>
        <div className="d-flex justify-content-end">
          <ColumnVisibilityControl table={table}/>
        </div>
        <div className="d-flex align-items-center justify-content-between">
          { basicTableLayout(table) }
        </div>
      </div>
    },
    {
      ...useParticipantDupeTab({ users, studyEnvContext, onUpdate: reload }),
      path: 'dupes'
    },
    {
      name: 'Account merging', path: 'merge', component: <ParticipantMergeView studyEnvContext={studyEnvContext}
        onUpdate={reload}/>
    }
  ]

  return <div className="container-fluid px-4 py-2">
    <NavBreadcrumb value={'participantUserList'}>accounts</NavBreadcrumb>
    <div className="d-flex align-items-center justify-content-between ">
      {renderPageHeader('Accounts')}

      <ParticipantListViewSwitcher
        studyEnvConfig={studyEnvContext.currentEnv.studyEnvironmentConfig}
      />
    </div>
    <LoadingSpinner isLoading={isLoading}>
      <div className="d-flex mb-2">
        { tabs.map(tab => {
          return <NavLink key={tab.path} to={tab.path} style={tabLinkStyle}>
            <div className="py-2 px-4">
              {tab.name}
            </div>
          </NavLink>
        })}
      </div>
      <Routes>
        { tabs.map(tab => <Route path={tab.path} key={tab.path} element={tab.component}/>)}
        <Route index element={<Navigate to={tabs[0].path} replace={true}/>}/>
      </Routes>

    </LoadingSpinner>
  </div>
}
