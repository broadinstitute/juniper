import { NavBreadcrumb } from 'navbar/AdminNavbar'
import React from 'react'
import { IconButton } from 'components/forms/Button'
import { faThumbtack } from '@fortawesome/free-solid-svg-icons'
import Select from 'react-select'
import { ENVIRONMENT_ICON_MAP } from './publishing/PortalPublishingView'
import { usePinnedEnv } from './usePinnedEnv'
import { useNavigate } from 'react-router-dom'

const envOpts = ['live', 'irb', 'sandbox'].map(env => ({
  label: <span>{ENVIRONMENT_ICON_MAP[env]} &nbsp; {env}</span>,
  value: env
}))

interface StudyEnvironmentSwitcherProps {
  currentEnvPath: string
  envName: string
  changeEnv: (newEnv?: string) => void
}

export const StudyEnvironmentSwitcher = ({
  currentEnvPath,
  envName
}: StudyEnvironmentSwitcherProps) => {
  const navigate = useNavigate()
  const { pinnedEnv, setPinnedEnv } = usePinnedEnv()

  const changeEnv = (newEnv?: string) => {
    if (!newEnv) {
      return
    }
    const currentPath = window.location.pathname
    const newPath = currentPath
      .replace(`/env/${envName}`, `/env/${newEnv}`)
    navigate(newPath)
  }

  const handlePinClick = () => {
    if (pinnedEnv) {
      setPinnedEnv(undefined)
    } else {
      setPinnedEnv(envName)
    }
    changeEnv()
  }

  return (
    <NavBreadcrumb value={currentEnvPath + pinnedEnv}>
      <IconButton
        icon={faThumbtack}
        aria-label={
          'Pin this study environment. Pinning an environment will keep it selected when navigating to other pages.'
        }
        tooltipPlacement={'bottom'} variant={pinnedEnv ? 'primary' : 'light'} className="me-2 border"
        onClick={handlePinClick}
      />
      <Select options={envOpts}
        value={envOpts.find(opt => opt.value === envName)}
        className="me-2"
        styles={{
          control: baseStyles => ({
            ...baseStyles,
            minWidth: '9em'
          })
        }}
        onChange={opt => changeEnv(opt?.value)}
      />
    </NavBreadcrumb>
  )
}
