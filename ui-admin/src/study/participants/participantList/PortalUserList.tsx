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
import _groupBy from 'lodash/groupBy'
import { tabLinkStyle } from 'util/subNavStyles'
import ParticipantMergeView from '../merge/ParticipantMergeView'
import ParticipantDupeView, { DupeType, UserDupe } from '../merge/ParticipantDupeView'
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
  const [possibleDupes, setPossibleDupes] = useState<UserDupe[]>()

  const { isLoading, reload } = useLoadingEffect(async () => {
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
    setPossibleDupes(identifyDupes(mappedResult))
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
      name: `Possible Duplicates ${possibleDupes ? `(${possibleDupes.length})` : ''}`, path: 'dupes',
      component: <ParticipantDupeView possibleDupes={possibleDupes || []} studyEnvContext={studyEnvContext}
        onUpdate={reload}/>
    },
    {
      name: 'Merge form', path: 'merge', component: <ParticipantMergeView studyEnvContext={studyEnvContext}
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
        <Route index element={<Navigate to={tabs[0].path} replace={true}/>}/>
        { tabs.map(tab => <Route path={tab.path} key={tab.path} element={tab.component}/>)}
      </Routes>

    </LoadingSpinner>
  </div>
}


const NO_DATA = 'no_data'
const DUPE_FUNCTIONS: {type: DupeType, func: (user: ParticipantUserWithEnrollees) => string}[] = [
  { type: 'username', func: (user: ParticipantUserWithEnrollees) => user.username.toLowerCase() },
  {
    type: 'name', func: (user: ParticipantUserWithEnrollees) => {
      if (user.enrollees.length === 0 ||
        !user.enrollees[0].profile.givenName && !user.enrollees[0].profile.familyName) {
        return NO_DATA
      }
      return `${user.enrollees[0].profile.givenName?.toLowerCase()} 
          ${user.enrollees[0].profile.familyName?.toLowerCase()}`
    }
  }
]

function identifyDupes(users: ParticipantUserWithEnrollees[]) {
  const possibleDupes: UserDupe[] = []
  DUPE_FUNCTIONS.forEach(dupeFunc => {
    const dupeGroups = _groupBy(users, dupeFunc.func)
    Object.keys(dupeGroups).forEach(dupeKey => {
      if (dupeGroups[dupeKey].length > 1 && dupeKey !== NO_DATA) {
        possibleDupes.push({
          users: dupeGroups[dupeKey],
          dupeType: dupeFunc.type
        })
      }
    })
  })
  return possibleDupes
}

