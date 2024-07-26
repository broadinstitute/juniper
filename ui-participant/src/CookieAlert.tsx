import { faCookieBite } from '@fortawesome/free-solid-svg-icons'
import React from 'react'
import { Alert, useI18n } from '@juniper/ui-core'

type CookieAlertProps = {
  onDismiss: () => void
}

export const CookieAlert = (props: CookieAlertProps) => {
  const { onDismiss } = props
  const { i18n } = useI18n()
  return (
    <Alert
      className="mb-1"
      icon={faCookieBite}
      level="WARNING"
      role="alert"
      style={{
        position: 'fixed',
        bottom: 0,
        left: '50%',
        transform: 'translate(-50%, 0)',
        width: 'calc(100vw - 1rem)',
        maxWidth: 1122
      }}
      title={i18n('cookieAlertTitle')}
      detail={i18n('cookieAlertDetail')}
      onDismiss={onDismiss}
    />
  )
}
