import React, {useEffect, useState} from 'react'
import Api, {getEnvSpec, EnvSpec, SiteContent, PortalEnvironment} from "api/api";


/** current user object context */
export const PortalEnvironmentContext = React.createContext<PortalEnvironment | null>(null)

/**
 * Provider for the current user object.
 * if a user object has already been obtained, it can be passed-in
 */
export default function PortalEnvrionmentProvider({ children }: { children: any}) {
  const [envState, setEnvState] = useState<PortalEnvironment | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [isError, setIsError] = useState(false)
  const envSpec: EnvSpec = getEnvSpec()

  useEffect( () => {
    Api.getPortalEnvironment(envSpec.shortname, envSpec.envName).then(result => {
      result.studyShortname = envSpec.shortname
      setEnvState(result)
      setIsError(false)
      setIsLoading(false)
    }).catch(error => {
      setIsError(true)
      setIsLoading(false)
    })
  }, [])

  return <>
    { isLoading && <div className="App-loading-background">
      <div className="position-absolute top-50 start-50 translate-middle">Loading...</div>
    </div> }
    { isError && <div className="App-loading-background">
      <div className="position-absolute top-50 start-50 translate-middle">
        The page you are looking for does not exist
      </div>
    </div>}
    { !isLoading && !isError && <PortalEnvironmentContext.Provider value={envState}>
      {children}
    </PortalEnvironmentContext.Provider> }
  </>
}


