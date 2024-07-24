import React, { useContext, useEffect, useState } from 'react'
import Api, { Config } from 'api/api'
import LoadingSpinner from 'util/LoadingSpinner'

const uninitializedConfig = {
  b2cTenantName: 'uninitialized',
  b2cClientId: 'uninitialized',
  b2cPolicyName: 'uninitialized',
  participantUiHostname: 'uninitialized',
  participantApiHostname: 'uninitialized',
  adminUiHostname: 'uninitialized',
  adminApiHostname: 'uninitialized',
  deploymentZone: 'uninitialized'
}

const ConfigContext = React.createContext<Config>(uninitializedConfig)

export const useConfig = () => useContext(ConfigContext)

export const ConfigConsumer = ConfigContext.Consumer

/**
 * Loads runtime config from API. Renders a loading spinner instead of children until config has been loaded.
 */
export default function ConfigProvider({ children }: { children: React.ReactNode }) {
  const [config, setConfig] = useState<Config>(uninitializedConfig)
  const [isLoaded, setIsLoaded] = useState(false)
  const [error, setError] = useState<string>()

  useEffect(() => {
    Api.getConfig().then(fetchedConfig => {
      setConfig(fetchedConfig)
      setIsLoaded(true)
    }).catch(e => {
      setError(`Unable to load config: ${e.toString()}`)
    })
  }, [])
  if (error) {
    return <p>{error}</p>
  } else {
    return <LoadingSpinner isLoading={!isLoaded}>
      <ConfigContext.Provider value={config}>
        { children }
      </ConfigContext.Provider>
    </LoadingSpinner>
  }
}
