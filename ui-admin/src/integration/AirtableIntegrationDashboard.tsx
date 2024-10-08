import React, { useState } from 'react'
import { useLoadingEffect } from 'api/api-utils'
import Api, { InternalConfig } from 'api/api'
import LoadingSpinner from 'util/LoadingSpinner'


/** shows basic config for airtable */
export default function AirtableIntegrationDashboard() {
  const [config, setConfig] = useState<InternalConfig>()

  const { isLoading } = useLoadingEffect(async () => {
    const response = await Api.fetchInternalConfig()
    setConfig(response)
  })

  return <div>
    <h2 className="h3">Airtable integration</h2>
    <div className="mt-4">
      {!isLoading && <dl>
        <dt>apiKey</dt>
        <dd>{config?.airtable.authToken}</dd>
      </dl>}
      {isLoading && <LoadingSpinner/>}
    </div>
  </div>
}
