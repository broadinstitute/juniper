import React, { useState } from 'react'
import _keyBy from 'lodash/keyBy'
import _mapValues from 'lodash/mapValues'
import { Link } from 'react-router-dom'
import {
  ColumnDef,
  ColumnFiltersState,
  getCoreRowModel,
  getFilteredRowModel,
  getSortedRowModel, SortingState,
  useReactTable,
  VisibilityState
} from '@tanstack/react-table'

import Api, { Enrollee } from 'api/api'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import {
  basicTableLayout,
  checkboxColumnCell,
  ColumnVisibilityControl,
  IndeterminateCheckbox,
  RowVisibilityCount
} from 'util/tableUtils'
import LoadingSpinner from 'util/LoadingSpinner'
import { instantToDateString } from 'util/timeUtils'
import RequestKitsModal from './RequestKitsModal'
import { useLoadingEffect } from 'api/api-utils'
import { enrolleeKitRequestPath } from '../participants/enrolleeView/EnrolleeView'
import { Button } from '../../components/forms/Button'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faDownload, faPaperPlane } from '@fortawesome/free-solid-svg-icons'

type EnrolleeRow = Enrollee & {
  taskCompletionStatus: Record<string, boolean>
}

/**
 * Interface for filtering/selecting enrollees who should receive sample kits.
 */
export default function KitEnrolleeSelection({ studyEnvContext }: { studyEnvContext: StudyEnvContextT }) {
  const { portal, study, currentEnv, currentEnvPath } = studyEnvContext
  const [enrollees, setEnrollees] = useState<EnrolleeRow[]>([])
  const [sorting, setSorting] = React.useState<SortingState>([
    { id: 'createdAt', desc: true },
    { id: 'optionalSurveys', desc: true }

  ])
  const [rowSelection, setRowSelection] = useState<Record<string, boolean>>({})
  const [columnVisibility, setColumnVisibility] = useState<VisibilityState>({})
  const [columnFilters, setColumnFilters] = useState<ColumnFiltersState>([
    { id: 'consented', value: true },
    { id: 'kitRequested', value: false },
    { id: 'requiredSurveysComplete', value: true }
  ])
  const [showRequestKitModal, setShowRequestKitModal] = useState(false)


  const { isLoading, reload } = useLoadingEffect(async () => {
    const enrollees = await Api.fetchEnrolleesWithKits(
      portal.shortcode, study.shortcode, currentEnv.environmentName)
    const enrolleeRows = enrollees.map(enrollee => {
      const taskCompletionStatus = _mapValues(
        _keyBy(enrollee.participantTasks, task => task.targetStableId),
        task => task.status === 'COMPLETE'
      )

      return { ...enrollee, taskCompletionStatus }
    })
    setEnrollees(enrolleeRows)
  }, [studyEnvContext.study.shortcode, studyEnvContext.currentEnv.environmentName])

  const onSubmit = async () => {
    setShowRequestKitModal(false)
    reload()
  }
  const enrolleesSelected = Object.keys(rowSelection)
    .filter(key => rowSelection[key])
    .map(key => enrollees[parseInt(key)].shortcode)
  const numSelected = enrolleesSelected.length
  const enableActionButtons = numSelected > 0

  const requiredSurveys = currentEnv.configuredSurveys.filter(survey => survey.required)
  const hasCompletedAllRequiredSurveys = (enrollee: Enrollee) => {
    return enrollee.participantTasks.filter(
      task => task.blocksHub && task.status === 'COMPLETE' && task.taskType === 'SURVEY'
    ).length === requiredSurveys.length
  }
  const optionalSurveysCompleted = (enrollee: Enrollee) => {
    return enrollee.participantTasks.filter(
      task => !task.blocksHub && task.status === 'COMPLETE' && task.taskType === 'SURVEY'
    ).length
  }


  const columns: ColumnDef<EnrolleeRow, string | boolean | number>[] = [{
    id: 'select',
    header: ({ table }) => <IndeterminateCheckbox
      checked={table.getIsAllRowsSelected()} indeterminate={table.getIsSomeRowsSelected()}
      onChange={table.getToggleAllRowsSelectedHandler()}/>,
    cell: ({ row }) => (
      <div className="px-1">
        <IndeterminateCheckbox
          checked={row.getIsSelected()} indeterminate={row.getIsSomeSelected()}
          onChange={row.getToggleSelectedHandler()} disabled={!row.getCanSelect()}/>
      </div>
    )
  }, {
    header: 'Enrollee shortcode',
    accessorKey: 'shortcode',
    meta: {
      columnType: 'string'
    },
    cell: data => <Link to={enrolleeKitRequestPath(currentEnvPath, data.getValue().toString())}>{data.getValue()}</Link>
  }, {
    header: 'Join date',
    accessorKey: 'createdAt',
    cell: data => instantToDateString(Number(data.getValue()))
  }, {
    header: 'Consented',
    accessorKey: 'consented',
    meta: {
      columnType: 'boolean',
      filterOptions: [
        { value: true, label: 'Consented' },
        { value: false, label: 'Not Consented' }
      ]
    },
    filterFn: 'equals',
    cell: checkboxColumnCell
  }, {
    header: 'Required surveys complete',
    id: 'requiredSurveysComplete',
    accessorFn: enrollee => hasCompletedAllRequiredSurveys(enrollee),
    meta: {
      columnType: 'boolean',
      filterOptions: [
        { value: true, label: 'Yes' },
        { value: false, label: 'No' }
      ]
    },
    filterFn: 'equals',
    cell: checkboxColumnCell
  }, {
    header: '# Optional surveys complete',
    id: 'optionalSurveys',
    enableColumnFilter: false,
    accessorFn: enrollee => optionalSurveysCompleted(enrollee)
  }, {
    header: 'Kit requested',
    id: 'kitRequested',
    accessorFn: enrollee => enrollee.kitRequests.length !== 0,
    meta: {
      columnType: 'boolean',
      filterOptions: [
        { value: true, label: 'Requested' },
        { value: false, label: 'Not Requested' }
      ]
    },
    filterFn: 'equals',
    cell: checkboxColumnCell
  }]

  const table = useReactTable({
    data: enrollees,
    columns,
    state: { columnVisibility, rowSelection, columnFilters, sorting },
    enableRowSelection: true,
    onSortingChange: setSorting,
    onRowSelectionChange: setRowSelection,
    onColumnVisibilityChange: setColumnVisibility,
    onColumnFiltersChange: setColumnFilters,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel(),
    getFilteredRowModel: getFilteredRowModel()
  })

  return <div className="row">
    <div className="col-12">
      <LoadingSpinner isLoading={isLoading}>
        <div className="d-flex align-items-center justify-content-between py-3">
          <div className="d-flex align-items-center">
            <RowVisibilityCount table={table}/>
          </div>
          <div className="d-flex">
            <Button onClick={() => { setShowRequestKitModal(true) }}
              variant="light" className="border m-1" disabled={!enableActionButtons}
              tooltip={enableActionButtons ? 'Send sample collection kit' : 'Select at least one participant'}>
              <FontAwesomeIcon icon={faPaperPlane} className="fa-lg"/> Send sample collection kit
            </Button>
            <ColumnVisibilityControl table={table}/>
            { showRequestKitModal && <RequestKitsModal
              studyEnvContext={studyEnvContext}
              onDismiss={() => setShowRequestKitModal(false)}
              enrolleeShortcodes={enrolleesSelected}
              onSubmit={onSubmit}/> }
          </div>
        </div>
        { basicTableLayout(table, { filterable: true }) }
        { enrollees.length === 0 &&
          <span className="d-flex justify-content-center text-muted fst-italic">No participants</span> }
      </LoadingSpinner>
    </div>
  </div>
}
