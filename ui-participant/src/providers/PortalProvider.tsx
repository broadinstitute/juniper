import React, { useContext, useEffect, useState } from 'react'
import Api, { LocalSiteContent, Portal, PortalEnvironment } from 'api/api'


/** current portal object context */
const PortalContext = React.createContext<PortalEnvContextT | null>(null)

export type PortalEnvContextT = {
  portal: Portal,
  portalEnv: PortalEnvironment,
  reloadPortal: () => void,
  localContent: LocalSiteContent
}

/** use the loaded portal.  Attempting to call this outside of PortalProvider children will throw an exception */
export function usePortalEnv(): PortalEnvContextT {
  const portalContext = useContext(PortalContext)
  if (!portalContext?.portal) {
    throw ('Portal environment not initialized')
  }
  // the api guarantees the first environment and first localizedSiteContents returned are the correct ones
  const portalEnv = portalContext.portal.portalEnvironments[0]
  // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
  const localContent = portalEnv.siteContent!.localizedSiteContents[0]

  return {
    portal: portalContext.portal,
    portalEnv,
    reloadPortal: portalContext.reloadPortal,
    localContent
  }
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
    reloadPortal()
  }, [])

  const reloadPortal = () => {
    const selectedLanguage = localStorage.getItem('selectedLanguage') || 'en'
    setIsLoading(true)
    Api.getPortal(selectedLanguage).then(result => {
      setEnvState(result)
      setIsError(false)
      setIsLoading(false)
    }).catch(() => {
      setIsError(true)
      setIsLoading(false)
    })
  }

  const portalEnv = envState && {
    portal: envState,
    portalEnv: envState.portalEnvironments[0],
    reloadPortal,
    localContent: envState.portalEnvironments[0].siteContent!.localizedSiteContents[0]
  }

  return <>
    {isLoading && <div className="bg-white h-100 w-100">
      <div className="position-absolute top-50 start-50 translate-middle">Loading...</div>
    </div>}
    {isError && <div className="bg-white h-100 w-100">
      <div className="position-absolute top-50 start-50 translate-middle text-center">
        There is no Juniper site configured for this url.<br/>
        If this is an error, contact <a href="mailto:support@juniper.terra.bio">support@juniper.terra.bio</a>.
      </div>
    </div>}
    {!isLoading && !isError && <PortalContext.Provider value={portalEnv}>
      {children}
    </PortalContext.Provider>}
  </>
}
