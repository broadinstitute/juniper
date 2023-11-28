import React, { useEffect, useState } from 'react'
import { NavLink, Outlet, Route, Routes, useNavigate } from 'react-router-dom'
import NotificationConfigTypeDisplay, { deliveryTypeDisplayMap } from './NotifcationConfigTypeDisplay'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faPlus } from '@fortawesome/free-solid-svg-icons/faPlus'
import { paramsFromContext, StudyEnvContextT } from '../StudyEnvironmentRouter'
import NotificationConfigView from './NotificationConfigView'
import { renderPageHeader } from 'util/pageUtils'
import { LoadedPortalContextT } from '../../portal/PortalProvider'
import { NotificationConfig } from '@juniper/ui-core'
import Api from 'api/api'
import { useLoadingEffect } from 'api/api-utils'
import LoadingSpinner from 'util/LoadingSpinner'
import CreateNotificationConfigModal from './CreateNotificationConfigModal'
import { navDivStyle, navLinkStyleFunc, navListItemStyle } from 'util/subNavStyles'
import CollapsableMenu from 'navbar/CollapsableMenu'

const CONFIG_GROUPS = [
  { title: 'Events', type: 'EVENT' },
  { title: 'Participant Reminders', type: 'TASK_REMINDER' },
  { title: 'Ad-hoc', type: 'AD_HOC' }
]

/** shows configuration of notifications for a study */
export default function NotificationContent({ studyEnvContext, portalContext }:
  {studyEnvContext: StudyEnvContextT, portalContext: LoadedPortalContextT}) {
  const currentEnv = studyEnvContext.currentEnv
  const navigate = useNavigate()
  const [configList, setConfigList] = useState<NotificationConfig[]>([])
  const [previousEnv, setPreviousEnv] = useState<string>(currentEnv.environmentName)
  const [showCreateModal, setShowCreateModal] = useState(false)

  const { isLoading, reload } = useLoadingEffect(async () => {
    const configList = await Api.findNotificationConfigsForStudyEnv(portalContext.portal.shortcode,
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

  const onCreate = (createdConfig: NotificationConfig) => {
    reload()
    navigate(`configs/${createdConfig.id}`)
    setShowCreateModal(false)
  }

  return <div className="container-fluid px-4 py-2">
    { renderPageHeader('Participant email configuration') }
    <div className="d-flex">
      {isLoading && <LoadingSpinner/>}
      {!isLoading && <div style={navDivStyle}>
        <ul className="list-unstyled">
          { CONFIG_GROUPS.map(group => <li style={navListItemStyle}>
            <CollapsableMenu header={group.title} headerClass="text-black" content={
              <ul className="list-unstyled p-2">
                { configList
                  .filter(config => config.notificationType === group.type)
                  .map(config => <li key={config.id} className="mb-2">
                    <div className="d-flex">
                      <NavLink to={`configs/${config.id}`} style={navLinkStyleFunc}>
                        <NotificationConfigTypeDisplay config={config}/>
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
            element={<NotificationConfigView studyEnvContext={studyEnvContext} portalContext={portalContext}/>}/>
        </Routes>
        <Outlet/>
      </div>
      { showCreateModal && <CreateNotificationConfigModal studyEnvParams={paramsFromContext(studyEnvContext)}
        onDismiss={() => setShowCreateModal(false)} onCreate={onCreate}
      /> }
    </div>
  </div>
}
