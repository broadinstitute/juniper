import { StudyEnvParams } from '../StudyEnvironmentRouter'
import {
  ENVIRONMENT_NAMES,
  EnvironmentName,
  StudyEnvironmentSurvey,
  StudyEnvironmentSurveyNamed
} from '@juniper/ui-core'
import React, { useState } from 'react'
import { Link } from 'react-router-dom'
import { Button, EllipsisDropdownButton } from '../../components/forms/Button'
import SurveyEnvironmentDetailModal from './SurveyEnvironmentDetailModal'

export type SurveyTableProps = {
  stableIds: string[],
  studyEnvParams: StudyEnvParams,
  configuredSurveys: StudyEnvironmentSurveyNamed[]
  setSelectedSurveyConfig: (config: StudyEnvironmentSurvey) => void
  showDeleteSurveyModal: boolean
  setShowDeleteSurveyModal: (show: boolean) => void
  showArchiveSurveyModal: boolean
  setShowArchiveSurveyModal: (show: boolean) => void
  updateConfiguredSurvey: (surveyConfig: StudyEnvironmentSurvey) => void
}


/**
 * shows a list of surveys and the version currently live in each environment
 */
export default function SurveyEnvironmentTable(props: SurveyTableProps) {
  if (props.stableIds.length === 0) {
    return <div className="fst-italic fw-light pb-3 ps-2">None</div>
  }
  return <table className="table table-striped table-bordered">
    <thead>
      <tr>
        <td></td>
        {ENVIRONMENT_NAMES.map(envName => <td key={envName} className="p-2">{envName}</td>)}
      </tr>
    </thead>
    <tbody>
      { props.stableIds.map(stableId => <SurveyListItem key={stableId} stableId={stableId} {...props}/>)}
    </tbody>
  </table>
}

type SurveyListItemProps = SurveyTableProps & {
  stableId: string
}

const SurveyListItem = (props: SurveyListItemProps) => {
  const [detailEnv, setDetailEnv] = useState<EnvironmentName>()
  const {
    stableId, configuredSurveys, studyEnvParams,
    setShowArchiveSurveyModal, showArchiveSurveyModal, setSelectedSurveyConfig, setShowDeleteSurveyModal,
    showDeleteSurveyModal
  } = props
  const surveyName = configuredSurveys.find(config => config.survey.stableId === stableId)?.survey?.name
  if (!surveyName) {
    return null
  }
  const liveVersion = configuredSurveys
    .filter(config => config.survey.stableId === stableId && config.envName === 'live')
    .map(config => config.survey.version)[0]

  return <tr>
    <td>
      <Link to={`surveys/${stableId}`}>
        {surveyName}
      </Link>
    </td>

    { ENVIRONMENT_NAMES.map(envName => {
      const envConfig = configuredSurveys
        .find(config => config.envName === envName && config.survey.stableId === stableId)
      return <td key={envName} >
        {envConfig && <div className="d-flex align-items-center">
          <Button variant="secondary" onClick={() => setDetailEnv(envName)}>
              v{envConfig.survey.version}
          </Button>
          { (envConfig.survey.version !== liveVersion) &&
            <span className="badge bg-dark-subtle text-black rounded-5 fw-normal"
              title="this version is not in the live environment">unpublished</span>}
          { (envName === 'sandbox') &&  <div className="nav-item dropdown ms-auto">
            <EllipsisDropdownButton aria-label="configure survey menu" className="ms-auto"/>
            <div className="dropdown-menu">
              <ul className="list-unstyled">
                <li>
                  <button className="dropdown-item"
                    onClick={() => setDetailEnv(envName)}>
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
      </td>
    })}
    { detailEnv && <SurveyEnvironmentDetailModal
      stableId={stableId}
      studyEnvParams={{ ...studyEnvParams, envName: detailEnv }}
      onDismiss={() => setDetailEnv(undefined)}
    />}
  </tr>
}

/**

  */
