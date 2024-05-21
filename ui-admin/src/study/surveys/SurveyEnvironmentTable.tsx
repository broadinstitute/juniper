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
import SurveyPublishModal from './SurveyPublishModal'

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
  stableId: string
  sandbox?: StudyEnvironmentSurvey
  irb?: StudyEnvironmentSurvey
  live?: StudyEnvironmentSurvey
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

  const rowInfo: SurveyEnvTableRow[] = useMemo(() => stableIds.map(stableId => {
    const info: SurveyEnvTableRow = {
      stableId,
      name: configuredSurveys.find(config => config.survey.stableId === stableId)?.survey?.name ?? ''
    }
    ENVIRONMENT_NAMES.forEach(envName => {
      info[envName] = configuredSurveys.find(config =>
        config.survey.stableId === stableId && config.envName === envName)
    })
    return info
  }), [dependencyString])

  const columns: ColumnDef<SurveyEnvTableRow>[] = useMemo(() => [{
    header: '',
    id: 'name',
    cell: ({ row }) => <Link to={`surveys/${row.original.stableId}`}>
      {row.original.name}
    </Link>
  },
  ...ENVIRONMENT_NAMES.map((envName): ColumnDef<SurveyEnvTableRow> => ({
    header: envName,
    enableSorting: false,
    id: envName,
    cell: ({ row }) => <SurveyTableEnvColumn rowInfo={row.original} {...props} envName={envName}/>
  }))], [dependencyString])

  const table = useReactTable({
    data: rowInfo,
    columns,
    enableRowSelection: true,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel()
  })

  return <div>
    { basicTableLayout(table) }
  </div>
}

type PublishCommand = {
  surveyConfig: StudyEnvironmentSurvey
  destinationEnv: EnvironmentName
}

/** show a survey and the version in each environment */
const SurveyTableEnvColumn = (props: SurveyTableProps & {rowInfo: SurveyEnvTableRow, envName: EnvironmentName}) => {
  const [showEnvDetail, setShowEnvDetail] = useState(false)
  const [publishCommand, setPublishCommand] = useState<PublishCommand>()
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
      { (envConfig.survey.version !== rowInfo.live?.survey?.version) &&
          <span className="badge bg-dark-subtle text-black rounded-5 fw-normal"
            title="this version is not in the live environment">not live</span>}
      <div className="nav-item dropdown ms-3">
        <EllipsisDropdownButton aria-label="configure survey menu" className="ms-auto"/>
        <div className="dropdown-menu">
          <ul className="list-unstyled">
            <li>
              <button className="dropdown-item"
                onClick={() => setShowEnvDetail(true)}>
                  See participant assignment
              </button>
            </li>
            { ENVIRONMENT_NAMES.map(destinationEnv => {
              if (!(destinationEnv == 'live' && envName == 'sandbox') &&
                rowInfo[envName]?.survey?.version != rowInfo[destinationEnv]?.survey?.version) {
                return <li className="pt-2" key={destinationEnv}><button className="dropdown-item"
                  onClick={() => {
                    setPublishCommand({
                      surveyConfig: rowInfo[envName]!,
                      destinationEnv
                    })
                  }}>
                  Publish to {destinationEnv}
                </button></li>
              }
              return null
            }) }
            { envName == 'sandbox' && <>
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
              </li> </>
            }

          </ul>
        </div>
      </div>
    </div>
    }
    {!envConfig && <span className="m-2 fst-italic fw-light">n/a</span>}
    { showEnvDetail && <SurveyEnvironmentDetailModal
      stableId={rowInfo.stableId}
      studyEnvParams={{ ...studyEnvParams, envName }}
      onDismiss={() => setShowEnvDetail(false)}
    />}
    { publishCommand && <SurveyPublishModal
      surveyName={rowInfo.name}
      destinationEnv={publishCommand.destinationEnv}
      sourceConfig={publishCommand.surveyConfig}
      destConfig={rowInfo[publishCommand.destinationEnv]}
      studyEnvParams={{ ...studyEnvParams, envName }}
      onDismiss={() => setPublishCommand(undefined)}
    />}
  </div>
}
