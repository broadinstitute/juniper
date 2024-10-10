import { Button } from 'components/forms/Button'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faList, faUserLarge, faUserLargeSlash } from '@fortawesome/free-solid-svg-icons'
import { faUsers } from '@fortawesome/free-solid-svg-icons/faUsers'
import React from 'react'
import { useNavigate } from 'react-router-dom'
import { StudyEnvironmentConfig } from '@juniper/ui-core'

const VIEWS = [{
  name: 'withdrawn',
  icon: faUserLargeSlash,
  path: '/withdrawn'
}, {
  name: 'family',
  icon: faUsers,
  path: '/withdrawn'
}, {
  name: 'participant',
  icon: faUserLarge,
  path: ''
}, {
  name: 'account',
  icon: faList,
  path: '/accounts'
}]

export const ParticipantListViewSwitcher = ({ studyEnvConfig }: {studyEnvConfig: StudyEnvironmentConfig}) => {
  const navigate = useNavigate()
  const availableViews = VIEWS.filter(view => view.name !== 'family' || studyEnvConfig.enableFamilyLinkage)
  return (
    <div className="btn-group border my-1">
      { availableViews.map(view => <Button variant='light' key={view.name}
        aria-label={`Switch to ${view.name} view`}
        className={`btn btn-sm btn-light`}
        tooltip={`Switch to ${view.name} view`}
        onClick={() => {
          navigate(`..${view.path}`)
        }}>
        <FontAwesomeIcon icon={view.icon}/>
      </Button>)}
    </div>
  )
}
