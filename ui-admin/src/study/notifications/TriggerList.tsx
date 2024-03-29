import React, { useEffect, useState } from 'react'
import { NavLink, Outlet, Route, Routes, useNavigate } from 'react-router-dom'
import TriggerTypeDisplay, { deliveryTypeDisplayMap } from './TriggerTypeDisplay'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faPlus } from '@fortawesome/free-solid-svg-icons/faPlus'
import { paramsFromContext, StudyEnvContextT } from '../StudyEnvironmentRouter'
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

const CONFIG_GROUPS = [
  { title: 'Events', type: 'EVENT' },
  { title: 'Participant Reminders', type: 'TASK_REMINDER' },
  { title: 'Ad-hoc', type: 'AD_HOC' }
]

/** shows configuration of notifications for a study */
export default function TriggerList({ studyEnvContext, portalContext }:
  {studyEnvContext: StudyEnvContextT, portalContext: LoadedPortalContextT}) {
  const currentEnv = studyEnvContext.currentEnv
  const navigate = useNavigate()
  const [configList, setConfigList] = useState<Trigger[]>([])
  const [previousEnv, setPreviousEnv] = useState<string>(currentEnv.environmentName)
  const [showCreateModal, setShowCreateModal] = useState(false)

  const { isLoading, reload } = useLoadingEffect(async () => {
    const configList = await Api.findTriggersForStudyEnv(portalContext.portal.shortcode,
      studyEnvContext.study.shortcode, currentEnv.environmentName)
    setConfigList(configList)
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
    navigate(`configs/${createdConfig.id}`)
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
          { CONFIG_GROUPS.map(group => <li style={navListItemStyle} key={group.title}>
            <CollapsableMenu header={group.title} headerClass="text-black" content={
              <ul className="list-unstyled p-2">
                { configList
                  .filter(config => config.triggerType === group.type)
                  .map(config => <li key={config.id} className="mb-2">
                    <div className="d-flex">
                      <NavLink to={`configs/${config.id}`} style={navLinkStyleFunc}>
                        <TriggerTypeDisplay config={config}/>
                        <span
                          className="text-muted fst-italic"> ({deliveryTypeDisplayMap[config.deliveryType]})</span>
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
          <Route path="configs/:configId"
            element={<TriggerView studyEnvContext={studyEnvContext}
              portalContext={portalContext} onDelete={onDelete}/>}/>
        </Routes>
        <Outlet/>
      </div>
      { showCreateModal && <CreateTriggerModal studyEnvParams={paramsFromContext(studyEnvContext)}
        onDismiss={() => setShowCreateModal(false)} onCreate={onCreate}
      /> }
    </div>
  </div>
}
