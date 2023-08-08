import React, { PropsWithChildren, ReactNode, useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import Api, { Portal } from 'api/api'
import LoadingSpinner from 'util/LoadingSpinner'
import AdminUserProvider from '../providers/AdminUserProvider'
import { emptyContextAlertFunction } from '../util/contextUtils'


export type PortalContextT = {
  updatePortal: (portal: Portal) => void
  reloadPortal: (shortcode: string) => Promise<Portal | null>,
  portal: Portal | null,
  isLoading: boolean,
  isError: boolean
}

export type LoadedPortalContextT = {
  updatePortal: (portal: Portal) => void
  reloadPortal: (shortcode: string) => Promise<Portal>
  portal: Portal
}

export type PortalParams = {
  portalShortcode: string,
  portalEnv: string,
}

export const PortalContext = React.createContext<PortalContextT>({
  updatePortal: emptyContextAlertFunction,
  reloadPortal: emptyContextAlertFunction,
  portal: null,
  isLoading: true,
  isError: false
})


/** routable component wrapper for PortalProvider */
export default function PortalProvider(props: PropsWithChildren) {
  const params = useParams<PortalParams>()
  const portalShortcode: string | undefined = params.portalShortcode
  if (!portalShortcode) {
    return <span>No portal selected</span>
  }

  return <RawPortalProvider shortcode={portalShortcode}>{props.children}</RawPortalProvider>
}


/** context provider for a portal object */
function RawPortalProvider({ shortcode, children }:
                       { shortcode: string, children: ReactNode}) {
  const [portalState, setPortalState] = useState<Portal | null>(null)
  const [isError, setIsError] = useState(false)
  const [isLoading, setIsLoading] = useState(true)

  /** update the portal object to sync its state with the server */
  function updatePortal(updatedPortal: Portal) {
    setPortalState(updatedPortal)
  }

  /** grabs the latest from the server and updates the portal object */
  function reloadPortal(shortcode: string): Promise<Portal> {
    setIsLoading(true)
    return Api.getPortal(shortcode).then(result => {
      setPortalState(result)
      setIsError(false)
      setIsLoading(false)
      return result
    }).catch(() => {
      setIsError(true)
      setIsLoading(false)
      setPortalState(null)
      throw new Error('error - portal could not be loaded')
    })
  }

  useEffect(() => {
    void reloadPortal(shortcode)
  }, [shortcode])

  const portalContext: PortalContextT  = {
    portal: portalState,
    updatePortal,
    reloadPortal,
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
    <AdminUserProvider portalShortcode={shortcode}>
      {children}
    </AdminUserProvider>
  </PortalContext.Provider>
}
