import React, { useContext, useEffect, useState } from 'react'
import { Portal } from '@juniper/ui-core'
import { emptyContextAlertFunction } from '../util/contextUtils'
import Api from '../api/api'
import { studyParticipantsPath } from '../portal/PortalRouter'
import { useNavigate } from 'react-router-dom'
import LoadingSpinner from '../util/LoadingSpinner'

export type NavContextT = {
  breadCrumbs: React.ReactNode[],
  setBreadCrumbs: React.Dispatch<React.SetStateAction<React.ReactNode[]>>
  portalList: Portal[]
  setPortalList: (portalList: Portal[]) => void
}

const NavContext = React.createContext<NavContextT>({
  breadCrumbs: [],
  setBreadCrumbs: emptyContextAlertFunction,
  portalList: [],
  setPortalList: emptyContextAlertFunction
})

/** wrapper function for using the nav context */
export const useNavContext = () => useContext(NavContext)

/** Provider for a general navigation -- current study and portal.  this blocks rendering of children
 * until the list of portals the user has access to is loaded.  Accordingly, this should only be used
 * inside protected routes, since it requires the user to already be logged in. */
export default function NavContextProvider({ children }: { children: React.ReactNode}) {
  const [breadCrumbs, setBreadCrumbs] = useState<React.ReactNode[]>([])
  const [portalList, setPortalList] = useState<Portal[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const navigate = useNavigate()

  const navState: NavContextT = {
    breadCrumbs, setBreadCrumbs, portalList, setPortalList
  }
  useEffect(() => {
    Api.getPortals().then(result => {
      setPortalList(result)
      /** if there's only one study, and the user is going to the homepage, direct to that study's participant page */
      if (result.length === 1 && result[0].portalStudies.length === 1 && window.location.pathname.length < 2) {
        const studyShortcode = result[0].portalStudies[0].study.shortcode
        navigate(studyParticipantsPath(result[0].shortcode, studyShortcode, 'live'), { replace: true })
      }
      setIsLoading(false)
    })
  }, [])
  return <NavContext.Provider value={navState}>
    { isLoading && <LoadingSpinner/> }
    { !isLoading && children }
  </NavContext.Provider>
}


