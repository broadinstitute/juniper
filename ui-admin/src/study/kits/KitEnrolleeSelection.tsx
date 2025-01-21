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

import Api, { EnrolleeSearchExpressionResult, ParticipantTask } from 'api/api'
import { paramsFromContext, StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import {
  basicTableLayout,
  ColumnVisibilityControl,
  IndeterminateCheckbox, renderEmptyMessage,
  RowVisibilityCount, checkboxColumnCell
} from 'util/tableUtils'
import LoadingSpinner from 'util/LoadingSpinner'
import { Enrollee, instantToDateString, KitType, StudyEnvParams } from '@juniper/ui-core'
import RequestKitsModal from './RequestKitsModal'
import { useLoadingEffect } from 'api/api-utils'
import { enrolleeKitRequestPath } from 'study/participants/enrolleeView/EnrolleeView'
import { Button } from 'components/forms/Button'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faPaperPlane, faQrcode } from '@fortawesome/free-solid-svg-icons'
import { enrolleeConsentedColumn } from 'util/participantSearchUtils'

type EnrolleeRow = EnrolleeSearchExpressionResult & {
  taskCompletionStatus: Record<string, boolean>
}

/**
 * Interface for filtering/selecting enrollees who should receive sample kits.
 */
export default function KitEnrolleeSelection({ studyEnvContext }: { studyEnvContext: StudyEnvContextT }) {
  const { portal, study, currentEnv, currentEnvPath } = studyEnvContext
  const [studyEnvKitTypes, setStudyEnvKitTypes] = useState<KitType[]>([])
  const [enrollees, setEnrollees] = useState<EnrolleeRow[]>([])
  const [sorting, setSorting] = React.useState<SortingState>([
    { id: 'enrollee.createdAt', desc: true },
    { id: 'optionalSurveys', desc: true }

  ])
  const [rowSelection, setRowSelection] = useState<Record<string, boolean>>({})
  const [columnVisibility, setColumnVisibility] = useState<VisibilityState>({})
  const [columnFilters, setColumnFilters] = useState<ColumnFiltersState>([
    { id: 'enrollee.consented', value: true },
    { id: 'requiredSurveysComplete', value: true }
  ])

  const [showRequestKitModal, setShowRequestKitModal] = useState(false)

  const { isLoading, reload } = useLoadingEffect(async () => {
    const studyEnvParams: StudyEnvParams = paramsFromContext(studyEnvContext)

    const [kitTypes, enrollees] = await Promise.all([
      Api.fetchKitTypes(studyEnvParams),
      Api.executeSearchExpression(portal.shortcode, study.shortcode, currentEnv.environmentName, '',
        { includes: ['tasks', 'kitRequests'] })
    ])

    setStudyEnvKitTypes(kitTypes)
    setColumnFilters(prevState => [
      ...prevState,
      ...kitTypes.map(kitType => ({
        id: `${kitType.name}KitRequested`, value: false
      }))
    ])

    const enrolleeRows: EnrolleeRow[] = enrollees.map(result => {
      const taskCompletionStatus = _mapValues(
        _keyBy(result.tasks, task => task.targetStableId),
        task => (task as ParticipantTask).status === 'COMPLETE'
      )
      return {
        ...result, taskCompletionStatus
      }
    })
    setEnrollees(enrolleeRows)
  }, [studyEnvContext.currentEnvPath])

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
    .map(key => enrollees[parseInt(key)].enrollee.shortcode)
  const numSelected = enrolleesSelected.length
  const enableActionButtons = numSelected > 0

  const requiredResearchSurveys = currentEnv.configuredSurveys
    .filter(studyEnvSurvey => studyEnvSurvey.survey.required && studyEnvSurvey.survey.surveyType === 'RESEARCH')
  const hasCompletedAllRequiredResearchSurveys = (enrollee: EnrolleeSearchExpressionResult) => {
    return enrollee.tasks.filter(
      task => task.blocksHub && task.status === 'COMPLETE' && task.taskType === 'SURVEY'
    ).length === requiredResearchSurveys.length
  }

  const optionalSurveysCompleted = (enrollee: EnrolleeSearchExpressionResult) => {
    return enrollee.tasks.filter(
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
    accessorKey: 'enrollee.shortcode',
    meta: {
      columnType: 'string'
    },
    cell: data => <Link to={enrolleeKitRequestPath(currentEnvPath, data.getValue().toString())}>{data.getValue()}</Link>
  }, {
    header: 'Join date',
    accessorKey: 'enrollee.createdAt',
    id: 'enrollee.createdAt',
    cell: data => instantToDateString(Number(data.getValue()))
  },
  enrolleeConsentedColumn(),
  {
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
  },
  ...studyEnvKitTypes.map(kitType => ({
    header: `${kitType.displayName} kit requested`,
    id: `${kitType.name}KitRequested`,
    accessorFn: (enrollee: Enrollee) => enrollee.kitRequests.some(request => request.kitType.name === kitType.name),
    meta: {
      columnType: 'boolean',
      filterOptions: [
        { value: true, label: 'Requested' },
        { value: false, label: 'Not Requested' }
      ]
    },
    cell: checkboxColumnCell
  }))]

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
