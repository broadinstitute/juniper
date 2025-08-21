import React, {
  useEffect,
  useState
} from 'react'
import _keyBy from 'lodash/keyBy'
import _mapValues from 'lodash/mapValues'
import { Link } from 'react-router-dom'
import {
  ColumnDef,
  ColumnFiltersState,
  getCoreRowModel,
  getFilteredRowModel,
  getSortedRowModel,
  SortingState,
  useReactTable,
  VisibilityState
} from '@tanstack/react-table'

import Api, {
  EnrolleeSearchExpressionResult,
  KeyedSearchValueTypeDefinition,
  ParticipantTask
} from 'api/api'
import {
  paramsFromContext,
  StudyEnvContextT
} from 'study/StudyEnvironmentRouter'
import {
  basicTableLayout,
  checkboxColumnCell,
  DownloadControl,
  IndeterminateCheckbox,
  renderEmptyMessage,
  RowVisibilityCount
} from 'util/table/tableUtils'
import LoadingSpinner from 'util/LoadingSpinner'
import {
  currentIsoDate,
  Enrollee,
  instantToDateString,
  KitType,
  StudyEnvParams
} from '@juniper/ui-core'
import RequestKitsModal from './RequestKitsModal'
import { useLoadingEffect } from 'api/api-utils'
import { enrolleeKitRequestPath } from 'study/participants/enrolleeView/EnrolleeView'
import { Button } from 'components/forms/Button'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import {
  faBoxOpen,
  faPaperPlane,
  faQrcode
} from '@fortawesome/free-solid-svg-icons'
import { useParticipantSearchState } from 'util/participantSearchUtils'
import {
  ColumnVisibilityControl,
  enrolleeConsentedColumn,
  getDynamicColumn
} from 'util/table/columnUtils'
import AssignKitModal from 'study/kits/AssignKitModal'
import { isNil } from 'lodash'
import ParticipantSearch from 'study/participants/participantList/search/ParticipantSearch'

type EnrolleeRow = EnrolleeSearchExpressionResult & {
  taskCompletionStatus: Record<string, boolean>
}

/**
 * Interface for filtering/selecting enrollees who should receive sample kits.
 */
export default function KitEnrolleeSelection({ studyEnvContext }: { studyEnvContext: StudyEnvContextT }) {
  const { portal, study, currentEnv, currentEnvPath } = studyEnvContext

  const customKitEligibilityRule = currentEnv.studyEnvironmentConfig.kitEligibilityRule
  const hasCustomKitEligibilityRule = !isNil(customKitEligibilityRule)

  const [studyEnvKitTypes, setStudyEnvKitTypes] = useState<KitType[]>([])
  const [enrollees, setEnrollees] = useState<EnrolleeRow[]>([])
  const [sorting, setSorting] = React.useState<SortingState>([
    { id: 'enrollee.createdAt', desc: true },
    { id: 'optionalSurveys', desc: true }
  ])

  const [rowSelection, setRowSelection] = useState<Record<string, boolean>>({})
  const [columnVisibility, setColumnVisibility] = useState<VisibilityState>({})
  // if no custom kit eligibility rule, add default filters
  const [columnFilters, setColumnFilters] = useState<ColumnFiltersState>(hasCustomKitEligibilityRule ? [] : [
    { id: 'enrollee.consented', value: true },
    { id: 'requiredSurveysComplete', value: true }
  ])

  const [showRequestKitModal, setShowRequestKitModal] = useState(false)
  const [showAssignKitModal, setShowAssignKitModal] = useState(false)

  const {
    searchState,
    setSearchState,
    updateSearchState,
    searchExpression,
    facets
  } = useParticipantSearchState(
    [],
    false,
    paramsFromContext(studyEnvContext),
    'kitSearch',
    {
      custom: hasCustomKitEligibilityRule ? customKitEligibilityRule : ''
    })


  const { isLoading, reload } = useLoadingEffect(async () => {
    const studyEnvParams: StudyEnvParams = paramsFromContext(studyEnvContext)

    const [kitTypes, enrollees] = await Promise.all([
      Api.fetchKitTypes(studyEnvParams),
      Api.executeSearchExpression(
        portal.shortcode,
        study.shortcode,
        currentEnv.environmentName,
        searchExpression,
        { includes: ['tasks', 'kitRequests'] })
    ])

    setStudyEnvKitTypes(kitTypes)
    setColumnFilters(prevState => [
      ...prevState,
      ...kitTypes.map(kitType => ({
        id: `${kitType.name}KitRequested`, value: false
      }))
    ])
    setRowSelection({})

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
  }, [studyEnvContext.currentEnvPath, searchExpression])

  const onSubmit = async (anyKitWasCreated: boolean) => {
    setShowRequestKitModal(false)
    reload()
    if (anyKitWasCreated) {
      /** if any kits were created, that changes the filter state of the table
       and could result in hidden items still being selected. Clear the selections to be safe */
      table.toggleAllRowsSelected(false)
    }
  }
  const selectedEnrollees = Object.keys(rowSelection)
    .filter(key => rowSelection[key])
    .map(key => {
      const searchExp = enrollees[parseInt(key)]

      const enrollee = searchExp.enrollee
      enrollee.profile = searchExp.profile
      return enrollee
    })

  const selectedEnrolleeShortcodes = selectedEnrollees.map(enrollee => enrollee.shortcode)

  const numSelected = selectedEnrolleeShortcodes.length
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
    meta: {
      columnType: 'instant'
    },
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
    meta: {
      columnType: 'number'
    },
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
  if (searchState.includeFacets) {
    searchState.includeFacets.forEach(facet => {
      columns.push(getDynamicColumn(facet))
    })
  }

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

  const dynamicColOpts = {
    facets,
    dynamicFacets: searchState?.includeFacets ?? [],
    setDynamicFacets: (dynamicFacets: KeyedSearchValueTypeDefinition[]) => setSearchState({
      ...searchState,
      includeFacetKeys: dynamicFacets.map(facet => facet.key)
    })
  }

  return <LoadingSpinner isLoading={isLoading}>
    <div className="d-flex align-items-center justify-content-between">
      <div className="flex-grow-1 d-flex align-content-center align-items-center justify-content-between">
        <ParticipantSearch
          key={currentEnv.environmentName}
          studyEnvContext={studyEnvContext}
          searchState={searchState}
          updateSearchState={updateSearchState}
          setSearchState={setSearchState}
          disabled={false}
          customLabels={{
            'custom': 'Kit Eligibility'
          }}
        />
      </div>
    </div>
    <div className="d-flex align-items-center justify-content-between">
      <div className="ps-2">
        <RowVisibilityCount table={table}/>
      </div>
      <div className="d-flex align-items-center">
        <Link to={'../scan'}>
          <Button variant="light" className="border m-1"><FontAwesomeIcon icon={faQrcode}/> Scan in-person kit</Button>
        </Link>
        <Button onClick={() => {
          setShowAssignKitModal(true)
        }}
        variant="light" className="border m-1" disabled={!enableActionButtons}
        tooltip={enableActionButtons
          ? 'Assign a sample kit to a participant for manual kit shipment'
          : 'Select at least one participant'}>
          <FontAwesomeIcon icon={faBoxOpen} className="fa-lg"/> Assign return-only kit
        </Button>
        <Button onClick={() => { setShowRequestKitModal(true) }}
          variant="light" className="border m-1" disabled={!enableActionButtons}
          tooltip={enableActionButtons
            ? 'Request a sample collection kit to be sent directly to the participant(s)'
            : 'Select at least one participant'}>
          <FontAwesomeIcon icon={faPaperPlane} className="fa-lg"/> Send sample collection kit
        </Button>
        <ColumnVisibilityControl table={table} dynamicColOpts={dynamicColOpts}/>
        <div>
          <DownloadControl table={table}
            fileName={`kits-${currentIsoDate()}`}/></div>
        {showRequestKitModal && <RequestKitsModal
          studyEnvContext={studyEnvContext}
          onDismiss={() => setShowRequestKitModal(false)}
          enrolleeShortcodes={selectedEnrolleeShortcodes}
          onSubmit={onSubmit}/> }

        {showAssignKitModal && selectedEnrollees.length > 0 && <AssignKitQueueModal
          studyEnvContext={studyEnvContext}
          onDismiss={() => {
            setShowAssignKitModal(false)
            reload()
          }}
          enrollees={selectedEnrollees}
        />}
      </div>
    </div>
    {basicTableLayout(table, { filterable: true })}
    {renderEmptyMessage(enrollees, 'No participants')}
  </LoadingSpinner>
}

const AssignKitQueueModal = ({
  studyEnvContext,
  enrollees,
  onDismiss
}: {
  studyEnvContext: StudyEnvContextT,
  enrollees: Enrollee[],
  onDismiss: () => void,
}) => {
  const [queueIndex, setQueueIndex] = useState(0)

  useEffect(() => {
    setQueueIndex(0)
  }, [enrollees])

  const incrementQueueIndex = () => {
    if (queueIndex < enrollees.length - 1) {
      setQueueIndex(queueIndex + 1)
    } else {
      onDismiss()
    }
  }

  return <AssignKitModal
    studyEnvContext={studyEnvContext}
    enrollee={enrollees[queueIndex]}
    onDismiss={onDismiss}
    onSubmit={incrementQueueIndex}
    skip={incrementQueueIndex}
    queueIdx={queueIndex}
    queueLength={enrollees.length}
  />
}
