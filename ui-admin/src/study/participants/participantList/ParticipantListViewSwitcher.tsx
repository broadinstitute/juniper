import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faList, faUserLarge, faUserLargeSlash } from '@fortawesome/free-solid-svg-icons'
import { faUsers } from '@fortawesome/free-solid-svg-icons/faUsers'
import React from 'react'
import { NavLink } from 'react-router-dom'
import { StudyEnvironmentConfig } from '@juniper/ui-core'

const VIEWS = [{
  name: 'participant',
  icon: faUserLarge,
  path: ''
},  {
  name: 'account',
  icon: faList,
  path: '/accounts'
}, {
  name: 'family',
  icon: faUsers,
  path: '/families'
}, {
  name: 'withdrawn',
  icon: faUserLargeSlash,
  path: '/withdrawn'
}]

export const ParticipantListViewSwitcher = ({ studyEnvConfig }: {studyEnvConfig: StudyEnvironmentConfig}) => {
  const availableViews = VIEWS.filter(view => view.name !== 'family' || studyEnvConfig.enableFamilyLinkage)
  return (
    <div className="btn-group border my-1">
      { availableViews.map(view => <NavLink to={`..${view.path}`}
        key={view.name}
        end
        aria-label={`Switch to ${view.name} view`}
        title={`Switch to ${view.name} view`}
        className="btn btn-sm btn-light"
      >
        <FontAwesomeIcon icon={view.icon}/>
      </NavLink>) }
    </div>
  )
}
