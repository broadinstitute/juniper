import React, { useContext, useState } from 'react'
import { StudyEnvParams } from 'study/StudyEnvironmentRouter'
import Modal from 'react-bootstrap/Modal'
import LoadingSpinner from 'util/LoadingSpinner'

import { EnvironmentName, StudyEnvironmentSurvey } from '@juniper/ui-core'
import Api, { PortalEnvironmentChange } from 'api/api'
import { emptyChangeSet, emptyStudyEnvChange } from 'portal/publish/PortalEnvDiffView'
import { doApiLoad } from '../../api/api-utils'
import { Store } from 'react-notifications-component'

import { successNotification } from '../../util/notifications'
import { PortalContext, PortalContextT } from '../../portal/PortalProvider'

/** renders a modal that allows publishing a single survey to a new environment */
const SurveyPublishModal = ({
  studyEnvParams, surveyName, destinationEnv, sourceConfig, destConfig, onDismiss
}: {
  studyEnvParams: StudyEnvParams, surveyName: string, sourceConfig: StudyEnvironmentSurvey,
  destConfig?: StudyEnvironmentSurvey, destinationEnv: EnvironmentName, onDismiss: () => void}) => {
  const [isLoading, setIsLoading] = useState(false)
  const { reloadPortal } = useContext(PortalContext) as PortalContextT
  const changeSet: PortalEnvironmentChange = {
    ...emptyChangeSet,
    studyEnvChanges: [
      {
        ...emptyStudyEnvChange,
        studyShortcode: studyEnvParams.studyShortcode,
        surveyChanges: {
          removedItems: [],
          // whether we're adding or changing something depends on whether the survey is already in the dest. env.
          addedItems: destConfig ? [] : [sourceConfig],
          changedItems: destConfig ? [
            {
              sourceId: sourceConfig.id,
              destId: destConfig.id,
              configChanges: [],
              documentChange: {
                changed: true,
                oldVersion: destConfig.survey.version,
                oldStableId: destConfig.survey.stableId,
                newVersion: sourceConfig.survey.version,
                newStableId: sourceConfig.survey.stableId
              }
            }
          ] : []
        }
      }
    ]
  }

  const publish = async () => {
    doApiLoad(async () => {
      await Api.applyEnvChanges(studyEnvParams.portalShortcode, destinationEnv, changeSet)
      Store.addNotification(successNotification(`${destinationEnv} environment updated`))
      await reloadPortal(studyEnvParams.portalShortcode)
      onDismiss()
    }, { setIsLoading })
  }

  return <Modal show={true} onHide={onDismiss}>
    <Modal.Header closeButton>
      <Modal.Title>Publish {surveyName}</Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <div className="mb-3">
        Confirm publishing version {sourceConfig.survey.version} to
        the <span className="fw-bold">{destinationEnv}</span> environment.
      </div>
      {destConfig && <div>This will replace version {destConfig.survey.version} in that environment.</div>}
      {!destConfig && <div>This survey is not current in the {destinationEnv} environment,
        and publishing will add it.</div>}
    </Modal.Body>
    <Modal.Footer>
      <LoadingSpinner isLoading={isLoading}>
        <button
          className="btn btn-primary"
          onClick={publish}
        >Ok</button>
        <button className="btn btn-secondary" onClick={onDismiss}>Cancel</button>
      </LoadingSpinner>
    </Modal.Footer>
  </Modal>
}

export default SurveyPublishModal
