import React, { useContext, useEffect, useState } from 'react'
import Api, { LocalSiteContent, Portal, PortalEnvironment } from 'api/api'


/** current portal object context */
const PortalContext = React.createContext<Portal | null>(null)

export type PortalEnvContextT = {
  portal: Portal,
  portalEnv: PortalEnvironment,
  localContent: LocalSiteContent
}

/** use the loaded portal.  Attempting to call this outside of PortalProvider children will throw an exception */
export function usePortalEnv(): PortalEnvContextT {
  const portal = useContext(PortalContext)
  if (!portal) {
    throw ('Portal environment not initialized')
  }
  // the api guarantees the first environment and first localizedSiteContents returned are the correct ones
  const portalEnv = portal.portalEnvironments[0]
  const localContent = portalEnv.siteContent.localizedSiteContents[0]
  return { portal, portalEnv, localContent }
}

/**
 * Provider for the current user object.
 * if a user object has already been obtained, it can be passed-in
 */
export default function PortalProvider({ children }: { children: React.ReactNode }) {
  const [envState, setEnvState] = useState<Portal | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [isError, setIsError] = useState(false)

  useEffect(() => {
    Api.getPortal().then(result => {
      setEnvState(result)
      setIsError(false)
      setIsLoading(false)
    }).catch(() => {
      setIsError(true)
      setIsLoading(false)
    })
  }, [])

  return <>
    {isLoading && <div className="bg-white h-100 w-100">
      <div className="position-absolute top-50 start-50 translate-middle">Loading...</div>
    </div>}
    {isError && <div className="bg-white h-100 w-100">
      <div className="position-absolute top-50 start-50 translate-middle">
        The page you are looking for does not exist
      </div>
    </div>}
    {!isLoading && !isError && <PortalContext.Provider value={envState}>
      {children}
    </PortalContext.Provider>}
  </>
}


