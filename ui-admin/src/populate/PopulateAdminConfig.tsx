import React, { useState } from 'react'
import Api from 'api/api'
import { failureNotification, successNotification } from '../util/notifications'
import { Store } from 'react-notifications-component'
import { PopulateButton } from './PopulateControls'

/** control for invoking the populate portal API */
export default function PopulateAdminConfig() {
  const [isLoading, setIsLoading] = useState(false)

  /** execute the command */
  const populate = async () => {
    setIsLoading(true)
    try {
      await Api.populateAdminConfig()
      Store.addNotification(successNotification('Populate succeeded'))
    } catch {
      Store.addNotification(failureNotification('Populate failed'))
    }
    setIsLoading(false)
  }
  return <form className="row">
    <h3 className="h5">Admin config</h3>
    <p>Repopulates the adminConfig, including e.g. the study staff welcome email. </p>
    <div className="d-flex flex-column row-gap-2">
      <PopulateButton onClick={populate} isLoading={isLoading}/>
    </div>
  </form>
}
