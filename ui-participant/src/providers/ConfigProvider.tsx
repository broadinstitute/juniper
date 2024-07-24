import React, { PropsWithChildren, useContext, useEffect, useState } from 'react'
import Api, { Config } from 'api/api'
import { PageLoadingIndicator } from 'util/LoadingSpinner'

const uninitializedConfig = {
  b2cTenantName: 'uninitialized',
  b2cClientId: 'uninitialized',
  b2cPolicyName: 'uninitialized',
  b2cChangePasswordPolicyName: 'uninitialized'
}

const ConfigContext = React.createContext<Config>(uninitializedConfig)

export const useConfig = () => useContext(ConfigContext)

export const ConfigConsumer = ConfigContext.Consumer

/**
 * Loads runtime config from API. Renders a loading spinner instead of children until config has been loaded.
 */
export default function ConfigProvider({ children }: PropsWithChildren) {
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
  } else if (!isLoaded) {
    return <PageLoadingIndicator />
  } else {
    return (
      <ConfigContext.Provider value={config}>
        { children }
      </ConfigContext.Provider>
    )
  }
}
