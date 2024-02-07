import React, { useState } from 'react'
import { useLoadingEffect } from 'api/api-utils'
import Api, { InternalConfig } from 'api/api'
import LoadingSpinner from 'util/LoadingSpinner'


/** shows controls and debug info for testing kit request (Pepper) integrations */
export default function AddressValidationIntegrationDashboard() {
  const [config, setConfig] = useState<InternalConfig>()

  const { isLoading } = useLoadingEffect(async () => {
    const response = await Api.fetchInternalConfig()
    setConfig(response)
  })

  return <div>
    <h2 className="h3">Address Validation</h2>
    <div className="mt-4">
      <h3 className="h5">Config</h3>
      {!isLoading && <dl>
        <dt>addressValidationClass</dt><dd>{config?.addressValidationConfig.addressValidationClass}</dd>
      </dl>}
      {isLoading && <LoadingSpinner/>}
    </div>
  </div>
}
