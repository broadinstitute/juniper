import React, {useEffect, useState} from 'react'
import {Link, NavLink, Outlet, Route, Routes, useNavigate} from 'react-router-dom'
import NotificationConfigTypeDisplay, { deliveryTypeDisplayMap } from './NotifcationConfigTypeDisplay'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faPlus } from '@fortawesome/free-solid-svg-icons/faPlus'
import {StudyEnvContextT, studyEnvPath} from '../StudyEnvironmentRouter'
import NotificationConfigView from "./NotificationConfigView";
import {LoadedPortalContextT} from "portal/PortalProvider";

const CONFIG_GROUPS = [
  {title: 'Event', type: 'EVENT'},
  {title: 'Reminder', type: 'TASK_REMINDER'},
  {title: 'Ad-hoc', type: 'AD_HOC'}
]

/** shows configuration of notifications for a study */
export default function NotificationContent({ studyEnvContext, portalContext }:
{studyEnvContext: StudyEnvContextT, portalContext: LoadedPortalContextT}) {
  const currentEnv = studyEnvContext.currentEnv
  const navigate = useNavigate()
  const [prevEnv, setPrevEnv] = useState<string>(currentEnv.environmentName)
  /** styles links as bold if they are the current path */
  const navStyleFunc = ({ isActive }: {isActive: boolean}) => {
    return isActive ? { fontWeight: 'bold' } : {}
  }

  const eventConfigs = currentEnv.notificationConfigs.filter(config => config.notificationType === 'EVENT')
  const reminderConfigs = currentEnv.notificationConfigs.filter(config => config.notificationType === 'REMINDER')
  const otherConfigs = currentEnv.notificationConfigs
      .filter(config => config.notificationType !== 'EVENT' && config.notificationType !== 'EVENT')

  useEffect(() => {
    if (prevEnv !== currentEnv.environmentName) {
      // the user has changed the environment -- we need to clear the id off the path if there
      navigate(`${studyEnvContext.currentEnvPath}/notificationContent`)
      setPrevEnv(currentEnv.environmentName)
    }
  }, [currentEnv.environmentName])

  return <div className="container-fluid">
    <div className="bg-white p-3 ps-5 row">
      <div className="col-md-3 px-0 py-3 mh-100 bg-white border-end">
        <h2 className="h5">Participant Notifications</h2>
        <ul className="list-unstyled px-2">
          { CONFIG_GROUPS.map(group => <li key={group.type}>
            <h3 className="h6">{group.title}</h3>
            <ul>
              { currentEnv.notificationConfigs
                  .filter(config => config.notificationType === group.type)
                  .map(config => <li key={config.id} className="py-1">
                    <div className="d-flex">
                      <NavLink to={`configs/${config.id}`} style={navStyleFunc}>
                        <NotificationConfigTypeDisplay config={config}/>
                        <span className="detail"> ({deliveryTypeDisplayMap[config.deliveryType]})</span>
                      </NavLink>
                    </div>
                  </li>
              ) }
            </ul>
          </li>)}
        </ul>
        <button className="btn btn-secondary" onClick={() => alert('not yet implemented')}>
          <FontAwesomeIcon icon={faPlus}/> Add
        </button>
      </div>
      <div className="col-md-9 py-3">
        <Routes>
          <Route path="configs/:configId"
                 element={<NotificationConfigView studyEnvContext={studyEnvContext} portalContext={portalContext}/>}/>
        </Routes>
        <Outlet/>
      </div>
    </div>
  </div>
}
