import React, { useEffect, useState } from 'react'
import { NavLink, Outlet, Route, Routes, useNavigate } from 'react-router-dom'
import TriggerTypeDisplay, { deliveryTypeDisplayMap } from './TriggerTypeDisplay'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faPlus } from '@fortawesome/free-solid-svg-icons/faPlus'
import { paramsFromContext, StudyEnvContextT, triggerPath } from '../StudyEnvironmentRouter'
import TriggerView from './TriggerView'
import { renderPageHeader } from 'util/pageUtils'
import { LoadedPortalContextT } from '../../portal/PortalProvider'
import { Trigger } from '@juniper/ui-core'
import Api from 'api/api'
import { useLoadingEffect } from 'api/api-utils'
import LoadingSpinner from 'util/LoadingSpinner'
import CreateTriggerModal from './CreateTriggerModal'
import { navDivStyle, navLinkStyleFunc, navListItemStyle } from 'util/subNavStyles'
import CollapsableMenu from 'navbar/CollapsableMenu'
import TriggerNotifications from './TriggerNotifications'

const TRIGGER_GROUPS = [
  { title: 'Events', type: 'EVENT' },
  { title: 'Participant Reminders', type: 'TASK_REMINDER' },
  { title: 'Ad-hoc', type: 'AD_HOC' }
]

/** shows configuration of notifications for a study */
export default function TriggerList({ studyEnvContext, portalContext }:
  {studyEnvContext: StudyEnvContextT, portalContext: LoadedPortalContextT}) {
  const currentEnv = studyEnvContext.currentEnv
  const navigate = useNavigate()
  const [triggers, setTriggers] = useState<Trigger[]>([])
  const [previousEnv, setPreviousEnv] = useState<string>(currentEnv.environmentName)
  const [showCreateModal, setShowCreateModal] = useState(false)

  const { isLoading, reload } = useLoadingEffect(async () => {
    const triggerList = await Api.findTriggersForStudyEnv(portalContext.portal.shortcode,
      studyEnvContext.study.shortcode, currentEnv.environmentName)
    setTriggers(triggerList)
  }, [currentEnv.environmentName, studyEnvContext.study.shortcode])

  useEffect(() => {
    if (previousEnv !== currentEnv.environmentName) {
      // the user has changed the environment -- we need to clear the id off the path if there
      navigate(`${studyEnvContext.currentEnvPath}/notificationContent`)
      setPreviousEnv(currentEnv.environmentName)
    }
  }, [currentEnv.environmentName])

  const onCreate = (createdConfig: Trigger) => {
    reload()
    navigate(triggerPath(createdConfig, studyEnvContext.currentEnvPath))
    setShowCreateModal(false)
  }

  const onDelete = () => {
    reload()
    navigate('')
  }

  return <div className="container-fluid px-4 py-2">
    { renderPageHeader('Participant email configuration') }
    <div className="d-flex">
      {isLoading && <LoadingSpinner/>}
      {!isLoading && <div style={navDivStyle}>
        <ul className="list-unstyled">
          { TRIGGER_GROUPS.map(group => <li style={navListItemStyle} key={group.title}>
            <CollapsableMenu header={group.title} headerClass="text-black" content={
              <ul className="list-unstyled p-2">
                { triggers
                  .filter(trigger => trigger.triggerType === group.type)
                  .map(trigger => <li key={trigger.id} className="mb-2">
                    <div className="d-flex">
                      <NavLink to={`triggers/${trigger.id}`} style={navLinkStyleFunc}>
                        <TriggerTypeDisplay config={trigger}/>
                        <span
                          className="text-muted fst-italic"> ({deliveryTypeDisplayMap[trigger.deliveryType]})</span>
                      </NavLink>
                    </div>
                  </li>
                  ) }
              </ul>}
            />
          </li>)}
          { currentEnv.environmentName == 'sandbox' && <li style={navListItemStyle} className="ps-3">
            <button className="btn btn-secondary" onClick={() => setShowCreateModal(true)}>
              <FontAwesomeIcon icon={faPlus}/> Add
            </button>
          </li> }
        </ul>
      </div> }
      <div className="flex-grow-1 bg-white p-3">
        <Routes>
          <Route path="triggers/:triggerId"
            element={<TriggerView studyEnvContext={studyEnvContext}
              portalContext={portalContext} onDelete={onDelete}/>}/>
          <Route path="triggers/:triggerId/notifications" element={
            <TriggerNotifications studyEnvContext={studyEnvContext}/>}/>
        </Routes>
        <Outlet/>
      </div>
      { showCreateModal && <CreateTriggerModal studyEnvParams={paramsFromContext(studyEnvContext)}
        onDismiss={() => setShowCreateModal(false)} onCreate={onCreate}
      /> }
    </div>
  </div>
}
