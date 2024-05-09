import React, { useState } from 'react'
import {
  ConfigChange,
  Portal,
  PortalEnvironmentChange, StudyEnvironmentChange
} from 'api/api'
import { Link } from 'react-router-dom'
import StudyEnvDiff from './StudyEnvDiff'
import { ConfigChangeListView, ConfigChanges, renderNotificationConfig, VersionChangeView } from './diffComponents'
import { faArrowRight } from '@fortawesome/free-solid-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import _cloneDeep from 'lodash/cloneDeep'

export const emptyChangeSet: PortalEnvironmentChange = {
  siteContentChange: { changed: false },
  configChanges: [],
  preRegSurveyChanges: { changed: false },
  triggerChanges: { addedItems: [], removedItems: [], changedItems: [] },
  participantDashboardAlertChanges: [],
  studyEnvChanges: []
}

export const emptyStudyEnvChange: StudyEnvironmentChange = {
  studyShortcode: '',
  configChanges: [],
  preEnrollSurveyChanges: { changed: false },
  surveyChanges: { addedItems: [], removedItems: [], changedItems: [] },
  triggerChanges: { addedItems: [], removedItems: [], changedItems: [] }
}

const EXCLUDED_PROPS = ['participantHostname']

/**
 * gets a default set of changes to apply for the changeset.
 * This will be empty if the target environment is already initialized, and everything if the target environment
 * is not yet initialized
 */
const getDefaultPortalEnvChanges = (changes: PortalEnvironmentChange) => {
  changes.configChanges = changes.configChanges
    .filter(configChange => !EXCLUDED_PROPS.includes(configChange.propertyName))
  const initializationChange = changes.configChanges.find(configChange => configChange.propertyName === 'initialized')
  if (initializationChange && initializationChange.oldValue === false) {
    return _cloneDeep(changes)
  }
  return {
    ...emptyChangeSet,
    participantDashboardAlertChanges: changes.participantDashboardAlertChanges.map(alertChange => (
      {
        trigger: alertChange.trigger,
        changes: []
      }
    )),
    studyEnvChanges: changes.studyEnvChanges.map(studyEnvChange => (
      {
        ...getDefaultStudyEnvChanges(studyEnvChange),
        studyShortcode: studyEnvChange.studyShortcode
      }
    ))
  }
}

/**
 * gets a default set of changes to apply for the changeset.
 * This will be empty if the target environment is already initialized, and everything if the target environment
 * is not yet initialized
 */
const getDefaultStudyEnvChanges = (changes: StudyEnvironmentChange) => {
  const initializationChange = changes.configChanges.find(configChange => configChange.propertyName === 'initialized')
  if (initializationChange && initializationChange.oldValue === false) {
    return _cloneDeep(changes)
  }
  return {
    ...emptyStudyEnvChange,
    studyShortcode: changes.studyShortcode
  }
}

type EnvironmentDiffProps = {
  portal: Portal,
  sourceEnvName: string,
  applyChanges:  (changeSet: PortalEnvironmentChange) => void,
  changeSet: PortalEnvironmentChange,
  destEnvName: string
}

/**
 * loads and displays the differences between two portal environments
 * */
export default function PortalEnvDiffView(
  { changeSet, destEnvName, applyChanges, sourceEnvName, portal }: EnvironmentDiffProps) {
  const [selectedChanges, setSelectedChanges] = useState<PortalEnvironmentChange>(getDefaultPortalEnvChanges(changeSet))

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
      {destEnvName}
    </h1>
    <span>Select changes to apply</span>

    <div className="bg-white p-3">
      <div className="my-2">
        <h2 className="h6">
          Environment config</h2>
        <div className="ms-4">
          <ConfigChanges configChanges={changeSet.configChanges}
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
        <div className="ms-4 ">
          { changeSet.siteContentChange.changed &&
              <label className="d-flex">
                <input type="checkbox" className="me-3" checked={selectedChanges.siteContentChange.changed}
                  onChange={e => setSelectedChanges({
                    ...selectedChanges,
                    siteContentChange: e.target.checked ? changeSet.siteContentChange : { changed: false }
                  })}/>
                <VersionChangeView record={changeSet.siteContentChange}/>
              </label>}
          { !changeSet.siteContentChange.changed && <VersionChangeView record={changeSet.siteContentChange}/> }
        </div>
      </div>
      <div className="my-2">
        <h2 className="h6">
          Participant dashboard alerts</h2>
        <div className="ms-4">
          { changeSet.participantDashboardAlertChanges.length > 0 ?
            changeSet.participantDashboardAlertChanges.map(alertChange => (
              <div key={alertChange.trigger} className="px-3 my-2 py-2" style={{ backgroundColor: '#ededed' }}>
                <h2 className="h5 pb-2">{alertChange.trigger}</h2>
                <ConfigChanges configChanges={alertChange.changes}
                  selectedChanges={selectedChanges.participantDashboardAlertChanges.find(change =>
                    change.trigger === alertChange.trigger)?.changes || []
                  }
                  updateSelectedChanges={(updatedConfigChanges: ConfigChange[]) => {
                    const updatedAlertChanges = selectedChanges.participantDashboardAlertChanges
                    const alertIndex = updatedAlertChanges.findIndex(change =>
                      change.trigger === alertChange.trigger)
                    updatedAlertChanges[alertIndex] = {
                      trigger: alertChange.trigger,
                      changes: updatedConfigChanges
                    }
                    setSelectedChanges({
                      ...selectedChanges,
                      participantDashboardAlertChanges: updatedAlertChanges
                    })
                  }}
                />
              </div>
            )) :
            <span className="fst-italic text-muted">no changes</span>
          }
        </div>
      </div>
      <div className="my-2">
        <h2 className="h6">
          Prereg survey
          <span className="fst-italic text-muted fs-6 ms-3">
            (Note this is pre-registration for the Portal as a whole, not a
            particular study)
          </span>
        </h2>
        <div className="ms-4">
          <VersionChangeView record={changeSet.preRegSurveyChanges}/>
        </div>
      </div>
      <div className="my-2">
        <h2 className="h6">
          Notification Configs</h2>
        <div className="ms-4">
          <ConfigChangeListView configChangeList={changeSet.triggerChanges}
            selectedChanges={selectedChanges.triggerChanges}
            setSelectedChanges={triggerChanges =>
              setSelectedChanges({ ...selectedChanges, triggerChanges })}
            renderItemSummary={renderNotificationConfig}/>
        </div>
      </div>
      <div>
        <h2 className="h6">Studies</h2>
        {changeSet.studyEnvChanges.map(studyEnvChange => {
          const matchedChange = selectedChanges.studyEnvChanges
            .find(change => change.studyShortcode === studyEnvChange.studyShortcode) as StudyEnvironmentChange
          const studyName = portal.portalStudies.find(portalStudy =>
            portalStudy.study.shortcode === studyEnvChange.studyShortcode)?.study.name || studyEnvChange.studyShortcode
          return <StudyEnvDiff key={studyEnvChange.studyShortcode} studyName={studyName} studyEnvChange={studyEnvChange}
            selectedChanges={matchedChange} setSelectedChanges={updateSelectedStudyEnvChanges}/>
        })}
      </div>
    </div>
    <div className="d-flex justify-content-center mt-2 pb-5">
      <button className="btn btn-primary" onClick={() => applyChanges(selectedChanges)}>Copy changes</button>
      {
        // eslint-disable-next-line
        // @ts-ignore  Link to type also supports numbers for back operations
        <Link className="btn btn-cancel" to={-1}>Cancel</Link>
      }
    </div>
  </div>
}

