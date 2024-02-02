import { StudyEnvParams } from '../StudyEnvironmentRouter'
import {
  ENVIRONMENT_NAMES,
  EnvironmentName,
  StudyEnvironmentSurvey,
  StudyEnvironmentSurveyNamed
} from '@juniper/ui-core'
import React, { useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { Button, EllipsisDropdownButton } from '../../components/forms/Button'
import SurveyEnvironmentDetailModal from './SurveyEnvironmentDetailModal'
import { ColumnDef, getCoreRowModel, getSortedRowModel, useReactTable } from '@tanstack/react-table'
import { basicTableLayout } from '../../util/tableUtils'

export type SurveyTableProps = {
  stableIds: string[], // the stableIds of surveys to show in the table
  studyEnvParams: StudyEnvParams,
  configuredSurveys: StudyEnvironmentSurveyNamed[]
  setSelectedSurveyConfig: (config: StudyEnvironmentSurvey) => void
  showDeleteSurveyModal: boolean
  setShowDeleteSurveyModal: (show: boolean) => void
  showArchiveSurveyModal: boolean
  setShowArchiveSurveyModal: (show: boolean) => void
  updateConfiguredSurvey: (surveyConfig: StudyEnvironmentSurvey) => void
}

type SurveyEnvTableRow = {
  stableId: string,
  liveVersion?: number,
  name: string
}
/**
 * shows a list of surveys and the version currently live in each environment
 */
export default function SurveyEnvironmentTable(props: SurveyTableProps) {
  const { stableIds, configuredSurveys } = props
  if (props.stableIds.length === 0) {
    return <div className="fst-italic fw-light pb-3 ps-2">None</div>
  }
  const dependencyString = stableIds.join('-')

  const rowInfo: SurveyEnvTableRow[] = useMemo(() => stableIds.map(stableId => ({
    stableId,
    name: configuredSurveys.find(config => config.survey.stableId === stableId)?.survey?.name ?? '',
    liveVersion: configuredSurveys.find(config =>
      config.survey.stableId === stableId && config.envName === 'live')?.survey?.version
  })), [dependencyString])

  const columns: ColumnDef<SurveyEnvTableRow>[] = useMemo(() => [{
    header: '',
    id: 'name',
    cell: ({ row }) => <Link to={`surveys/${row.original.stableId}`}>
      {row.original.name}
    </Link>
  },
  ...ENVIRONMENT_NAMES.map((envName): ColumnDef<SurveyEnvTableRow> => ({
    header: envName,
    id: envName,
    cell: ({ row }) => <SurveyTableEnvColumn rowInfo={row.original} {...props} envName={envName}/>
  }))], [dependencyString])

  const table = useReactTable({
    data: rowInfo,
    columns,
    enableRowSelection: true,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel(),
    debugTable: true
  })

  return <div>
    { basicTableLayout(table) }
  </div>
}

/** show a survey and the version in each environment */
const SurveyTableEnvColumn = (props: SurveyTableProps & {rowInfo: SurveyEnvTableRow, envName: EnvironmentName}) => {
  const [showEnvDetail, setShowEnvDetail] = useState(false)
  const {
    configuredSurveys, studyEnvParams,
    setShowArchiveSurveyModal, showArchiveSurveyModal, setSelectedSurveyConfig, setShowDeleteSurveyModal,
    showDeleteSurveyModal, rowInfo, envName
  } = props
  if (!rowInfo.name) {
    return null
  }

  const envConfig = configuredSurveys
    .find(config => config.envName === envName && config.survey.stableId === rowInfo.stableId)
  return <div>
    {envConfig && <div className="d-flex align-items-center">
      <Button variant="secondary" onClick={() => setShowEnvDetail(true)}>
            v{envConfig.survey.version}
      </Button>
      { (envConfig.survey.version !== rowInfo.liveVersion) &&
          <span className="badge bg-dark-subtle text-black rounded-5 fw-normal"
            title="this version is not in the live environment">unpublished</span>}
      { (envName === 'sandbox') &&  <div className="nav-item dropdown ms-auto">
        <EllipsisDropdownButton aria-label="configure survey menu" className="ms-auto"/>
        <div className="dropdown-menu">
          <ul className="list-unstyled">
            <li>
              <button className="dropdown-item"
                onClick={() => setShowEnvDetail(true)}>
                  See participant assignment
              </button>
            </li>
            <li className="pt-2">
              <button className="dropdown-item"
                onClick={() => {
                  setShowArchiveSurveyModal(!showArchiveSurveyModal)
                  setSelectedSurveyConfig(envConfig)
                }}>
                    Archive
              </button>
            </li>
            <li className="pt-2">
              <button className="dropdown-item"
                onClick={() => {
                  setShowDeleteSurveyModal(!showDeleteSurveyModal)
                  setSelectedSurveyConfig(envConfig)
                }}>
                    Delete
              </button>
            </li>
          </ul>
        </div>
      </div> }
    </div>
    }
    {!envConfig && <span className="m-2 fst-italic fw-light">n/a</span>}
    { showEnvDetail && <SurveyEnvironmentDetailModal
      stableId={rowInfo.stableId}
      studyEnvParams={{ ...studyEnvParams, envName }}
      onDismiss={() => setShowEnvDetail(false)}
    />}
  </div>
}
