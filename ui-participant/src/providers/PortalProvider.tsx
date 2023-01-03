import React, { useEffect, useState } from 'react'
import Api, { getEnvSpec, EnvSpec, Portal } from 'api/api'


/** current user object context */
export const PortalContext = React.createContext<Portal | null>(null)

/**
 * Provider for the current user object.
 * if a user object has already been obtained, it can be passed-in
 */
export default function PortalProvider({ children }: { children: React.ReactNode}) {
  const [envState, setEnvState] = useState<Portal | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [isError, setIsError] = useState(false)
  const envSpec: EnvSpec = getEnvSpec()

  useEffect(() => {
    Api.getPortal(envSpec.shortcode, envSpec.envName).then(result => {
      setEnvState(result)
      setIsError(false)
      setIsLoading(false)
    }).catch(() => {
      setIsError(true)
      setIsLoading(false)
    })
  }, [])

  return <>
    { isLoading && <div className="bg-white h-100 w-100">
      <div className="position-absolute top-50 start-50 translate-middle">Loading...</div>
    </div> }
    { isError && <div className="bg-white h-100 w-100">
      <div className="position-absolute top-50 start-50 translate-middle">
        The page you are looking for does not exist
      </div>
    </div>}
    { !isLoading && !isError && <PortalContext.Provider value={envState}>
      {children}
    </PortalContext.Provider> }
  </>
}


