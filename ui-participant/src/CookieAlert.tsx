import { faCookieBite } from '@fortawesome/free-solid-svg-icons'
import React from 'react'
import { Alert } from '@juniper/ui-core'

type CookieAlertProps = {
  onDismiss: () => void
}

// TODO: Add JSDoc
// eslint-disable-next-line jsdoc/require-jsdoc
export const CookieAlert = (props: CookieAlertProps) => {
  const { onDismiss } = props
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
      title="Cookies"
      detail="This site uses internet tokens called &quot;cookies&quot; to enable the
        proper functioning and security of our website, and to improve your
        experience while you use it. All of the cookies we use are strictly
        necessary cookies. This type of cookie does not collect any personally
        identifiable information about you and does not track your browsing
        habits. To learn more, [read our Privacy Policy](/privacy)."
      onDismiss={onDismiss}
    />
  )
}
