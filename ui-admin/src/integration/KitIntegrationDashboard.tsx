import React, { useState } from 'react'
import InfoPopup from 'components/forms/InfoPopup'
import { Button } from 'components/forms/Button'
import { doApiLoad, useLoadingEffect } from 'api/api-utils'
import Api, { InternalConfig } from 'api/api'
import { Store } from 'react-notifications-component'
import { successNotification } from 'util/notifications'
import LoadingSpinner from 'util/LoadingSpinner'


/** shows controls and debug info for testing kit request (Pepper) integrations */
export default function KitIntegrationDashboard() {
  const [isStatusSyncing, setIsStatusSyncing] = useState(false)
  const [config, setConfig] = useState<InternalConfig>()

  const syncStatuses = async () => {
    doApiLoad(async () => {
      await Api.refreshKitStatuses('ourhealth', 'ourheart', 'sandbox')
      Store.addNotification(successNotification('kit status sync succeeded'))
    }, {
      setIsLoading: setIsStatusSyncing,
      customErrorMsg: 'kit statuses could not be synced'
    })
  }

  const { isLoading } = useLoadingEffect(async () => {
    const response = await Api.fetchInternalConfig()
    setConfig(response)
  })

  return <div>
    <h2 className="h3">Kits</h2>
    <div className="mt-4">
      <h3 className="h5">Config</h3>
      {!isLoading && <dl>
        <dt>useLiveDsm</dt><dd>{config?.pepperDsmConfig.useLiveDsm ? 'true' : 'false'}</dd>
        <dt>issuerClaim</dt><dd>{config?.pepperDsmConfig.issuerClaim}</dd>
        <dt>basePath</dt><dd>{config?.pepperDsmConfig.basePath}</dd>
        <dt>secret</dt><dd>{config?.pepperDsmConfig.secret}</dd>
      </dl>}
      {isLoading && <LoadingSpinner/>}
    </div>
    <div className="mt-4">
      <h3 className="h5">Test</h3>
      kit status sync &nbsp;
      {isStatusSyncing && <LoadingSpinner/> }
      {!isStatusSyncing && <Button variant="primary" onClick={syncStatuses}>Test</Button>}
      <InfoPopup content={'Sends a request to sync OurHealth sandbox kit requests'}/>
    </div>
  </div>
}
