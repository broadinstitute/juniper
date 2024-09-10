import React, { useState } from 'react'
import {
  ConfigChange,
  Portal,
  PortalEnvironmentChange, StudyEnvironmentChange
} from 'api/api'
import { Link } from 'react-router-dom'
import StudyEnvDiff from './StudyEnvDiff'
import {
  ConfigChangeListView,
  ConfigChanges,
  renderNotificationConfig,
  renderPortalLanguage, valuePresent,
  VersionChangeView
} from './diffComponents'
import { faArrowRight } from '@fortawesome/free-solid-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import _cloneDeep from 'lodash/cloneDeep'
import { userHasPermission, useUser } from 'user/UserProvider'
import { Button } from 'components/forms/Button'
import { isEmpty } from 'lodash'
import Modal from 'react-bootstrap/Modal'
import LoadingSpinner from 'util/LoadingSpinner'

export const emptyChangeSet: PortalEnvironmentChange = {
  siteContentChange: { changed: false },
  configChanges: [],
  preRegSurveyChanges: { changed: false },
  triggerChanges: { addedItems: [], removedItems: [], changedItems: [] },
  participantDashboardAlertChanges: [],
  studyEnvChanges: [],
  languageChanges: { addedItems: [], removedItems: [], changedItems: [] }
}

export const emptyStudyEnvChange: StudyEnvironmentChange = {
  studyShortcode: '',
  configChanges: [],
  preEnrollSurveyChanges: { changed: false },
  surveyChanges: { addedItems: [], removedItems: [], changedItems: [] },
  triggerChanges: { addedItems: [], removedItems: [], changedItems: [] },
  kitTypeChanges: { addedItems: [], removedItems: [], changedItems: [] }
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
  const [showConfirmModal, setShowConfirmModal] = useState(false)
  const { user } = useUser()
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
    { userHasPermission(user, portal.id, 'publish') &&
      <span>Select changes to publish</span>
    }

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
      <div className="my-2">
        <h2 className="h6">
          Portal languages</h2>
        <div className="ms-4">
          <ConfigChangeListView configChangeList={changeSet.languageChanges}
            selectedChanges={selectedChanges.languageChanges}
            setSelectedChanges={languageChanges =>
              setSelectedChanges({ ...selectedChanges, languageChanges })}
            renderItemSummary={renderPortalLanguage}/>
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
      { userHasPermission(user, portal.id, 'publish') && <>
        <Button variant="primary" onClick={() => {
          // check for portal env and study env config changes. if empty, immediately apply changes
          if (isEmpty(selectedChanges.configChanges) &&
              selectedChanges.studyEnvChanges.every(studyChange => isEmpty(studyChange.configChanges))) {
            applyChanges(selectedChanges)
          } else {
            setShowConfirmModal(true)
          }
        }}>
          Publish changes to {destEnvName}
        </Button>
        {
          // eslint-disable-next-line
          // @ts-ignore  Link to type also supports numbers for back operations
          <Link className="btn btn-cancel" to={-1}>Back</Link>
        }
      </> }
      { !userHasPermission(user, portal.id, 'publish') &&
         <div>
           To publish selected changes, you must have the &quot;publish&quot; permission, or contact support
         </div>
      }
      { showConfirmModal &&
        <ConfirmConfigChangesModal portal={portal} selectedChanges={selectedChanges} applyChanges={applyChanges}
          onDismiss={() => setShowConfirmModal(false)}/>
      }
    </div>
  </div>
}

const ConfirmConfigChangesModal = ({ portal, selectedChanges, applyChanges, onDismiss }: {
  portal: Portal
  selectedChanges: PortalEnvironmentChange,
  applyChanges: (changeSet: PortalEnvironmentChange) => void,
  onDismiss: () => void
}) => {
  return <Modal className="modal-lg" show={true} onHide={onDismiss}>
    <Modal.Header closeButton>
      <Modal.Title>Confirm Publish</Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <div className="mb-2">
        Some of the changes that you are about to publish may significantly impact study operations.
        Please review the following items and confirm that you want to proceed:
      </div>

      <SensitiveConfigChangeList configChanges={selectedChanges.configChanges} title="Portal Configuration"/>

      <div className="mt-2">
        {selectedChanges.studyEnvChanges.map(studyChanges => {
          const studyName = portal.portalStudies.find(portalStudy =>
            portalStudy.study.shortcode === studyChanges.studyShortcode)?.study.name || studyChanges.studyShortcode

          return <SensitiveConfigChangeList
            key={studyChanges.studyShortcode}
            configChanges={studyChanges.configChanges}
            title={`${studyName} Study Configuration`}/>
        })}
      </div>

      <div className="mt-3">Are you sure that you want to proceed?</div>
    </Modal.Body>
    <Modal.Footer>
      <LoadingSpinner isLoading={false}>
        <button className="btn btn-primary" onClick={() => {
          applyChanges(selectedChanges)
        }}>Publish</button>
        <button className="btn btn-secondary" onClick={() => {
          onDismiss()
        }}>Cancel</button>
      </LoadingSpinner>
    </Modal.Footer>
  </Modal>
}

const SensitiveConfigChangeList = ({ configChanges, title }: {
  configChanges: ConfigChange[],
  title: string
}) => {
  const noVal = <span className="text-muted fst-italic">none</span>
  return configChanges.length > 0 ? (
    <>
      <label className="d-flex h4 mt-1">{title}</label>
      {configChanges.map(configChange => (
        <div className="d-flex" key={configChange.propertyName}>
          <div className="fw-semibold">{configChange.propertyName}:</div>
          <div className="ms-2">
            {valuePresent(configChange.oldValue) ? configChange.oldValue.toString() : noVal}
            <FontAwesomeIcon icon={faArrowRight} className="mx-2 fa-sm"/>
            {valuePresent(configChange.newValue) ? configChange.newValue.toString() : noVal}
          </div>
        </div>
      ))}
    </>
  ) : null
}

