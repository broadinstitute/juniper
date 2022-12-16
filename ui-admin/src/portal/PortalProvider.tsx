import React, { useEffect, useState } from 'react'
import { Outlet, useParams } from 'react-router-dom'
import Api, { Portal } from 'api/api'
import LoadingSpinner from 'util/LoadingSpinner'


export type PortalContextT = {
  updatePortal: (portal: Portal) => void
  portal: Portal | null,
  isLoading: boolean,
  isError: boolean
}

export type PortalParams = {
  portalShortname: string,
  portalEnv: string,
}

export const PortalContext = React.createContext<PortalContextT>({
  updatePortal: () => alert('error - portal not yet loaded'),
  portal: null,
  isLoading: true,
  isError: false
})

/** routable component wrapper for PortalProvider */
export default function RoutablePortalProvider() {
  const params = useParams<PortalParams>()
  const portalShortname: string | undefined = params.portalShortname

  if (!portalShortname) {
    return <span>No study selected</span>
  }
  return <PortalProvider shortname={portalShortname}>
    <Outlet/>
  </PortalProvider>
}


/** context provider for a portal object */
function PortalProvider({ shortname, children }:
                       { shortname: string, children: React.ReactNode}) {
  const [portalState, setPortalState] = useState<Portal | null>(null)
  const [isError, setIsError] = useState(false)
  const [isLoading, setIsLoading] = useState(true)

  /** update the portal object to sync its state with the server */
  function updatePortal(updatedPortal: Portal) {
    setPortalState(updatedPortal)
  }

  useEffect(() => {
    Api.getPortal(shortname).then(result => {
      setPortalState(result)
      setIsLoading(false)
    }).catch(() => {
      setIsError(true)
      setIsLoading(false)
      setPortalState(null)
    })
  }, [shortname])

  if (isError) {
    return <div>Portal &quot;{shortname}&quot; could not be loaded or found.</div>
  }

  if (isLoading) {
    return <div>
      <div className="text-center mt-5">
        Loading portal...  <LoadingSpinner/>
      </div>
    </div>
  }

  const portalContext: PortalContextT  = {
    portal: portalState,
    updatePortal,
    isLoading,
    isError
  }

  return <PortalContext.Provider value={portalContext}>
    { children }
  </PortalContext.Provider>
}
