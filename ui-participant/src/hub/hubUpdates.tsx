import React from 'react'
import { useLocation } from 'react-router-dom'

import { Alert, AlertProps } from '@juniper/ui-core'

export type HubUpdateMessage = {
  detail?: string
  icon?: AlertProps['icon']
  title: AlertProps['title']
  type: AlertProps['level']
}

export type HubUpdate = {
  message: HubUpdateMessage
}

/**
 * Pull any messages to be displayed as a result of where we came from e.g. "survey complete"
 * This is in accord with recommended usage of location state with React Router v6.
 */
export const useHubUpdate = (): HubUpdate | undefined => {
  const location = useLocation()
  return location.state as HubUpdate | undefined
}

type HubMessageAlertProps = { message: HubUpdateMessage } & Omit<AlertProps, 'children' | 'level' | 'title'>

export const HubMessageAlert = (props: HubMessageAlertProps) => {
  const { message, ...otherProps } = props
  return (
    <Alert
      level={message.type}
      icon={message.icon}
      title={message.title}
      detail={message.detail}
      {...otherProps}
    />
  )
}
