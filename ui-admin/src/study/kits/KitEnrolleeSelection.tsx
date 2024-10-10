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

import Api from 'api/api'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import {
  basicTableLayout,
  checkboxColumnCell,
  ColumnVisibilityControl,
  IndeterminateCheckbox, renderEmptyMessage,
  RowVisibilityCount
} from 'util/tableUtils'
import LoadingSpinner from 'util/LoadingSpinner'
import { Enrollee, instantToDateString } from '@juniper/ui-core'
import RequestKitsModal from './RequestKitsModal'
import { useLoadingEffect } from 'api/api-utils'
import { enrolleeKitRequestPath } from 'study/participants/enrolleeView/EnrolleeView'
import { Button } from 'components/forms/Button'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faPaperPlane, faQrcode } from '@fortawesome/free-solid-svg-icons'

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

  const onSubmit = async (anyKitWasCreated: boolean) => {
    setShowRequestKitModal(false)
    reload()
    if (anyKitWasCreated) {
      /** if any kits were created, that changes the filter state of the table
       and could result in hidden items still being selected. Clear the selections to be safe */
      table.toggleAllRowsSelected(false)
    }
  }
  const enrolleesSelected = Object.keys(rowSelection)
    .filter(key => rowSelection[key])
    .map(key => enrollees[parseInt(key)].shortcode)
  const numSelected = enrolleesSelected.length
  const enableActionButtons = numSelected > 0

  const requiredResearchSurveys = currentEnv.configuredSurveys
    .filter(studyEnvSurvey => studyEnvSurvey.survey.required && studyEnvSurvey.survey.surveyType === 'RESEARCH')
  const hasCompletedAllRequiredResearchSurveys = (enrollee: Enrollee) => {
    return enrollee.participantTasks.filter(
      task => task.blocksHub && task.status === 'COMPLETE' && task.taskType === 'SURVEY'
    ).length === requiredResearchSurveys.length
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
    accessorFn: enrollee => hasCompletedAllRequiredResearchSurveys(enrollee),
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

  return <LoadingSpinner isLoading={isLoading}>
    <div className="d-flex align-items-center justify-content-between">
      <div className="d-flex align-items-center">
        <RowVisibilityCount table={table}/>
      </div>
      <div className="d-flex">
        <Link to={'../scan'}>
          <Button variant="light" className="border m-1"><FontAwesomeIcon icon={faQrcode}/> Scan kit</Button>
        </Link>
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
    { renderEmptyMessage(enrollees, 'No participants') }
  </LoadingSpinner>
}
