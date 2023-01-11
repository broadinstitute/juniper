/** returns a notification config object suitable for passing to ReactNotification Store.addNotification */
import React from 'react'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faCheckCircle, faExclamationTriangle } from '@fortawesome/free-solid-svg-icons'
import { iNotification } from 'react-notifications-component'

/** returns a notification config object suitable for passing to ReactNotification Store.addNotification */
export function successNotification(message: React.ReactNode): iNotification {
  return {
    type: 'success',
    insert: 'top',
    container: 'top-right',
    title: '',
    message: <><FontAwesomeIcon icon={faCheckCircle}/>{message}</>,
    width: 425,
    dismiss: {
      duration: 3000,
      showIcon: false
    }
  }
}


/** returns a notification config object suitable for passing to ReactNotification Store.addNotification */
export function failureNotification(message: React.ReactNode, autodismiss = false): iNotification {
  const notification: iNotification = {
    ...successNotification(message),
    type: 'danger',
    message: <><FontAwesomeIcon icon={faExclamationTriangle}/>{message}</>
  }
  if (!autodismiss) {
    notification.dismiss = {
      duration: 0,
      showIcon: true
    }
  }
  return notification
}
