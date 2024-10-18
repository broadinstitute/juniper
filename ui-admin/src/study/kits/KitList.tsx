import React, { useState } from 'react'
import _capitalize from 'lodash/capitalize'
import _fromPairs from 'lodash/fromPairs'
import _groupBy from 'lodash/groupBy'
import { Link, Navigate, NavLink, Route, Routes } from 'react-router-dom'
import {
  ColumnDef,
  getCoreRowModel,
  getFilteredRowModel,
  getSortedRowModel,
  SortingState,
  useReactTable,
  VisibilityState
} from '@tanstack/react-table'

import Api from 'api/api'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import LoadingSpinner from 'util/LoadingSpinner'
import { basicTableLayout, ColumnVisibilityControl, renderEmptyMessage } from 'util/tableUtils'
import { instantToDateString, KitRequest } from '@juniper/ui-core'
import { doApiLoad, useLoadingEffect } from 'api/api-utils'
import { enrolleeKitRequestPath } from 'study/participants/enrolleeView/EnrolleeView'
import KitStatusCell from 'study/participants/KitStatusCell'
import { Button } from 'components/forms/Button'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faRefresh } from '@fortawesome/free-solid-svg-icons'
import { successNotification } from 'util/notifications'
import { Store } from 'react-notifications-component'
import { useUser } from 'user/UserProvider'
import { prettifyString, KitRequestDetails } from 'study/participants/KitRequests'
import { useAdminUserContext } from 'providers/AdminUserProvider'
import { tabLinkStyle } from '../../util/subNavStyles'

type KitStatusTabConfig = {
  statuses: string[],
  key: string,
  additionalColumns?: string[]
}

/**
 * Default column visibility, showing only columns relevant for any status. All columns should be listed here since
 * columns to hide must be explicitly configured.
 */
const defaultColumns: VisibilityState = {
  'enrolleeShortcode': true,
  'kitType_displayName': true,
  'createdAt': true,
  'labeledAt': false,
  'trackingNumber': false,
  'sentAt': false,
  'returnTrackingNumber': false,
  'creatingAdminUserId': false,
  'collectingAdminUserId': false,
  'kitLabel': false,
  'receivedAt': false,
  'status': false,
  'distributionMethod': false
}

/**
 * List of status tab properties in the order that they should apper on screen.
 */
const statusTabs: KitStatusTabConfig[] = [
  {
    statuses: ['CREATED'],
    key: 'created',
    additionalColumns: ['distributionMethod']
  },
  {
    statuses: ['QUEUED'],
    key: 'queued',
    additionalColumns: [
      'labeledAt', 'trackingNumber'
    ]
  },
  {
    statuses: ['SENT'],
    key: 'sent',
    additionalColumns: [
      'labeledAt', 'trackingNumber',
      'sentAt', 'returnTrackingNumber'
    ]
  },
  {
    statuses: ['COLLECTED_BY_STAFF'],
    key: 'collected',
    additionalColumns: [
      'creatingAdminUserId',
      'collectingAdminUserId',
      'returnTrackingNumber'
    ]
  },
  {
    statuses: ['RECEIVED'],
    key: 'returned',
    additionalColumns: [
      'labeledAt', 'trackingNumber',
      'sentAt', 'returnTrackingNumber',
      'receivedAt', 'distributionMethod'
    ]
  },
  {
    statuses: ['ERRORED', 'UNKNOWN'],
    key: 'issues',
    additionalColumns: [
      'labeledAt', 'trackingNumber',
      'sentAt', 'returnTrackingNumber',
      'receivedAt',
      'status'
    ]
  },
  {
    statuses: ['DEACTIVATED'],
    key: 'deactivated',
    additionalColumns: [
      'labeledAt', 'trackingNumber',
      'sentAt', 'returnTrackingNumber',
      'receivedAt',
      'status'
    ]
  }
]

/**
 * Determines a relevant set of columns to display based on the tab being rendered.
 * Some columns are always relevant, i.e. enrollee shortcode, kit type, and created date, and are represented in
 * `defaultColumns`. Additional columns to display are listed in KitStatusTabConfig.additionalColumns.
 */
const initialColumnVisibility = (tab: KitStatusTabConfig): VisibilityState => {
  return {
    ...defaultColumns,
    ..._fromPairs(tab.additionalColumns?.map(c => { return [c, true] }))
  }
}

/** Loads sample kits for a study and shows them as a list. */
export default function KitList({ studyEnvContext }: { studyEnvContext: StudyEnvContextT }) {
  const { portal, study, currentEnv } = studyEnvContext
  const [isRefreshing, setIsRefreshing] = useState(false)
  const [kits, setKits] = useState<KitRequest[]>([])
  const { user } = useUser()
  const { isLoading, reload } = useLoadingEffect(async () => {
    const kits= await Api.fetchKitsByStudyEnvironment(portal.shortcode, study.shortcode, currentEnv.environmentName)
    setKits(kits)
  }, [studyEnvContext.study.shortcode, studyEnvContext.currentEnv.environmentName])

  const kitsByTabKey = _groupBy(kits, kit => {
    return statusTabs.find(tab => tab.statuses.includes(kit.status))?.key || 'issues'
  })

  const refreshStatuses = async () => {
    doApiLoad(async () => {
      await Api.refreshKitStatuses(portal.shortcode, study.shortcode, currentEnv.environmentName)
      await reload()
      Store.addNotification(successNotification('kit statuses refreshed'))
    }, {
      setIsLoading: setIsRefreshing,
      customErrorMsg: 'kit statuses could not be refreshed'
    })
  }

  return <LoadingSpinner isLoading={isLoading}>
    <div className="container-fluid p-0 mt-2">
      <div className="d-flex w-100 align-items-center mb-2" style={{ backgroundColor: '#F5F8FF' }}>
        { statusTabs.map(tab => {
          const kits = kitsByTabKey[tab.key] || []
          return <NavLink key={tab.key} to={tab.key} style={tabLinkStyle}>
            <div className="py-2 px-4">
              {kits?.length} {_capitalize(tab.key)}
            </div>
          </NavLink>
        })}
        <div className="ms-auto">
          {user?.superuser && <Button variant="secondary" onClick={refreshStatuses}>
            {!isRefreshing && <span>Refresh <FontAwesomeIcon icon={faRefresh}/></span>}
            {isRefreshing && <LoadingSpinner/>}
          </Button> }
        </div>
      </div>
      <Routes>
        <Route index element={<Navigate to={statusTabs[0].key} replace={true}/>}/>
        { statusTabs.map(tab => {
          return <Route key={tab.key} path={tab.key} element={
            <KitListView
              studyEnvContext={studyEnvContext}
              tab={tab.key}
              kits={kitsByTabKey[tab.key] || []}
              initialColumnVisibility={initialColumnVisibility(tab)}/>
          }/>
        })}
      </Routes>
    </div>
  </LoadingSpinner>
}

/** Renders a table with a list of kits. */
function KitListView({ studyEnvContext, tab, kits, initialColumnVisibility }: {
  studyEnvContext: StudyEnvContextT,
  tab: string
  kits: KitRequest[],
  initialColumnVisibility: VisibilityState
}) {
  const { currentEnvPath } = studyEnvContext
  const [currentTab, setCurrentTab] = useState(tab)
  const [columnVisibility, setColumnVisibility] = useState<VisibilityState>(initialColumnVisibility)
  const [sorting, setSorting] = useState<SortingState>([])
  const { users } = useAdminUserContext()

  if (tab !== currentTab) {
    setColumnVisibility(initialColumnVisibility)
    setCurrentTab(tab)
  }

  const columns: ColumnDef<KitRequest, string>[] = [{
    header: 'Enrollee shortcode',
    accessorKey: 'enrolleeShortcode',
    meta: {
      columnType: 'string'
    },
    cell: data => <Link to={enrolleeKitRequestPath(currentEnvPath, data.getValue().toString())}>
      {data.getValue()}
    </Link>,
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
    header: 'Labeled',
    accessorKey: 'labeledAt',
    cell: data => instantToDateString(Number(data.getValue())),
    enableColumnFilter: false
  }, {
    header: 'Tracking Number',
    accessorKey: 'trackingNumber',
    enableColumnFilter: false
  }, {
    header: 'Kit Label',
    accessorKey: 'kitLabel',
    enableColumnFilter: false
  }, {
    header: 'Distribution Method',
    accessorKey: 'distributionMethod',
    enableColumnFilter: false,
    accessorFn: data => prettifyString(data.distributionMethod)
  }, {
    header: 'Requested By',
    accessorKey: 'creatingAdminUserId',
    cell: data => users.find(user => user.id === data.getValue())?.username,
    enableColumnFilter: false
  }, {
    header: 'Collected By',
    accessorKey: 'collectingAdminUserId',
    cell: data => users.find(user => user.id === data.getValue())?.username,
    enableColumnFilter: false
  }, {
    header: 'Sent',
    accessorKey: 'sentAt',
    cell: data => instantToDateString(Number(data.getValue())),
    enableColumnFilter: false
  }, {
    header: 'Return Tracking Number',
    accessorKey: 'returnTrackingNumber',
    enableColumnFilter: false
  }, {
    header: 'Returned',
    accessorKey: 'receivedAt',
    cell: data => instantToDateString(Number(data.getValue())),
    enableColumnFilter: false
  }, {
    header: 'Status',
    accessorKey: 'status',
    cell: data => <KitStatusCell kitRequest={data.row.original} infoPlacement='left'/>,
    enableColumnFilter: false
  }, {
    header: 'Details',
    accessorKey: 'details',
    cell: ({ row }) => <KitRequestDetails kitRequest={row.original}/>,
    enableColumnFilter: false
  }]

  const table = useReactTable({
    data: kits,
    columns,
    state: { columnVisibility, sorting },
    onColumnVisibilityChange: setColumnVisibility,
    onSortingChange: setSorting,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel(),
    getFilteredRowModel: getFilteredRowModel()
  })

  return <>
    <div className="d-flex align-items-center justify-content-between">
      <ColumnVisibilityControl table={table}/>
    </div>
    { basicTableLayout(table, { filterable: true }) }
    { renderEmptyMessage(kits, `No kits with status ${tab}`) }
  </>
}
