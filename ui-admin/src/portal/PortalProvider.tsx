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

export type LoadedPortalContextT = {
  updatePortal: (portal: Portal) => void
  portal: Portal
}

export type PortalParams = {
  portalShortcode: string,
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
  const portalShortcode: string | undefined = params.portalShortcode

  if (!portalShortcode) {
    return <span>No portal selected</span>
  }
  return <PortalProvider shortname={portalShortcode}>
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

  const portalContext: PortalContextT  = {
    portal: portalState,
    updatePortal,
    isLoading,
    isError
  }

  if (portalContext.isError) {
    return <div>Portal could not be loaded or found.</div>
  }

  if (portalContext.isLoading) {
    return <div>
      <div className="text-center mt-5">
        Loading portal...  <LoadingSpinner/>
      </div>
    </div>
  }

  return <PortalContext.Provider value={portalContext}>
    { children }
  </PortalContext.Provider>
}
