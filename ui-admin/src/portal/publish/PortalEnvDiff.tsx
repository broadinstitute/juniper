import React, { useEffect, useState } from 'react'
import Api, {
  ConfigChange, ListChange,
  Portal,
  PortalEnvironment,
  PortalEnvironmentChange, StudyEnvironmentChange, VersionedConfigChange, VersionedEntityChange
} from 'api/api'
import { Link, useNavigate, useParams } from 'react-router-dom'
import LoadingSpinner from 'util/LoadingSpinner'
import StudyEnvDiff from './StudyEnvDiff'
import { ConfigChangeListView, ConfigChanges, renderNotificationConfig, VersionChangeView } from './diffComponents'
import { faArrowRight } from '@fortawesome/free-solid-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { failureNotification, successNotification } from 'util/notifications'
import { Store } from 'react-notifications-component'
import { portalHomePath } from '../PortalRouter'

type EnvironmentDiffProps = {
  portal: Portal,
  portalEnv: PortalEnvironment,
}

const emptyChangeSet: PortalEnvironmentChange = {
  siteContentChange: {changed: false},
  configChanges: [],
  preRegSurveyChanges: {changed: false},
  notificationConfigChanges: {addedItems: [], removedItems: [], changedItems: []},
  studyEnvChanges: []
}

const emptyStudyEnvChange: StudyEnvironmentChange = {
  studyShortcode: '',
  configChanges: [],
  preEnrollSurveyChanges: {changed: false},
  consentChanges: {addedItems: [], removedItems: [], changedItems: []},
  surveyChanges: {addedItems: [], removedItems: [], changedItems: []},
  notificationConfigChanges: {addedItems: [], removedItems: [], changedItems: []}
}

/**
 * loads and displays the differences between two portal environments
 * */
export default function PortalEnvDiff({ portal, portalEnv }: EnvironmentDiffProps) {
  const params = useParams()
  const sourceEnvName: string | undefined = params.sourceEnvName
  const [isLoading, setIsLoading] = useState(true)
  const [diffResult, setDiffResult] = useState<PortalEnvironmentChange | null>(null)
  const [selectedChanges, setSelectedChanges] = useState<PortalEnvironmentChange>(emptyChangeSet)
  const navigate = useNavigate()

  useEffect(() => {
    if (!sourceEnvName) {
      alert('no source environment specified')
      return
    }
    Api.fetchEnvDiff(portal.shortcode, sourceEnvName, portalEnv.environmentName).then(result => {
      setDiffResult(result)
      setSelectedChanges({
        ...selectedChanges,
        studyEnvChanges: result.studyEnvChanges.map((studyEnvChange) => ({
          ...emptyStudyEnvChange,
          studyShortcode: studyEnvChange.studyShortcode
        }))
      })
      setIsLoading(false)
    }).catch(e => {
      alert(e)
      setIsLoading(false)
    })
  }, [])

  const applyChanges = () => {
    if (!diffResult) {
      return
    }
    Api.applyEnvChanges(portal.shortcode, portalEnv.environmentName, selectedChanges).then(() => {
      Store.addNotification(successNotification(`${portalEnv.environmentName} environment updated`))
      // TODO dynamically update state and/or reload page
      navigate(`/${portal.shortcode}`)
    }).catch(e => {
      Store.addNotification(failureNotification(`Update failed: ${  e.message}`))
    })
  }

  const updateSelectedStudyEnvChanges = (update: StudyEnvironmentChange) => {
    const matchedIndex = selectedChanges.studyEnvChanges
      .findIndex(change => change.studyShortcode === update.studyShortcode)
    const updatedChanges = [...selectedChanges.studyEnvChanges]
    updatedChanges[matchedIndex] = update
    setSelectedChanges({
      ...selectedChanges,
      studyEnvChanges: updatedChanges
    })
  }

  return <div className="container mt-3">
    <h1 className="h4">
      Difference: {sourceEnvName}
      <FontAwesomeIcon icon={faArrowRight} className="fa-sm mx-2"/>
      {portalEnv.environmentName}
    </h1>
    <span>Select changes to apply</span>
    <LoadingSpinner isLoading={isLoading}/>
    {diffResult && <>
      <div className="bg-white p-3">
        <div className="my-2">
          <h2 className="h6">
            Environment config</h2>
          <div className="ms-4">
            <ConfigChanges configChanges={diffResult.configChanges}
                           selectedChanges={selectedChanges.configChanges}
                           updateSelectedChanges={(updatedConfigChanges: ConfigChange[]) => setSelectedChanges({
                             ...selectedChanges,
                             configChanges: updatedConfigChanges
                           })}/>
          </div>
        </div>
        <div className="my-2">
          <h2 className="h6">
            Site content</h2>
          <div className="ms-4">
            <VersionChangeView record={diffResult.siteContentChange}/>
          </div>
        </div>
        <div className="my-2">
          <h2 className="h6">
            Prereg survey
            <span className="fst-italic text-muted fs-6 ms-3">
              Note this is pre-registration for the Portal as a whole, not a
              particular study
            </span>
          </h2>
          <div className="ms-4">
            <VersionChangeView record={diffResult.preRegSurveyChanges}/>
          </div>
        </div>
        <div className="my-2">
          <h2 className="h6"><input className="me-2" type={'checkbox'} checked={true} readOnly={true}/>
            Notification Configs</h2>
          <div className="ms-4">
            <ConfigChangeListView configChangeList={diffResult.notificationConfigChanges}
              renderItemSummary={renderNotificationConfig}/>
          </div>
        </div>
        <div>
          <h2 className="h6">Studies</h2>
          {diffResult.studyEnvChanges.map(studyEnvChange => {
            const matchedChange = selectedChanges.studyEnvChanges
                .find(change => change.studyShortcode === studyEnvChange.studyShortcode) as StudyEnvironmentChange
            return <StudyEnvDiff key={studyEnvChange.studyShortcode} studyEnvChange={studyEnvChange}
              selectedChanges={matchedChange} setSelectedChanges={updateSelectedStudyEnvChanges}/>
          })}
        </div>
      </div>
      <div className="d-flex justify-content-center mt-2 pb-5">
        <button className="btn btn-primary" onClick={applyChanges}>Copy changes</button>
        <Link className="btn btn-cancel" to={portalHomePath(portal.shortcode)}>Cancel</Link>
      </div>
    </>
    }
  </div>
}
