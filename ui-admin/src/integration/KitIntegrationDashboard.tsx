import React, { useState } from 'react'
import InfoPopup from '../components/forms/InfoPopup'
import { Button } from '../components/forms/Button'
import { doApiLoad } from '../api/api-utils'
import Api from '../api/api'
import { Store } from 'react-notifications-component'
import { successNotification } from 'util/notifications'
import LoadingSpinner from 'util/LoadingSpinner'

/** shows controls and debug info for testing kit request (Pepper) integrations */
export default function KitIntegrationDashboard() {
  const [isStatusSyncing, setIsStatusSyncing] = useState(false)
  const syncStatuses = async () => {
    doApiLoad(async () => {
      await Api.refreshKitStatuses('ourhealth', 'ourheart', 'sandbox')
      Store.addNotification(successNotification('kit status sync succeeded'))
    }, {
      setIsLoading: setIsStatusSyncing,
      customErrorMsg: 'kit statuses could not be synced'
    })
  }

  return <div>
    <div>
            Test kit status sync &nbsp;
      {isStatusSyncing && <LoadingSpinner/> }
      {!isStatusSyncing && <Button variant="primary" onClick={syncStatuses}>Test</Button>}
      <InfoPopup content={'Sends a request to sync OurHealth sandbox kit requests'}/>
    </div>
  </div>
}
