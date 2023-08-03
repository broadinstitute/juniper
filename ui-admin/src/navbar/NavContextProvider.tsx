import React, {useContext, useState} from 'react'
import {Portal, Study} from "@juniper/ui-core";
import {emptyContextAlertFunction} from "../util/contextUtils";

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

/** Provider for a general navigation -- current study and portal */
export default function NavContextProvider({ children }: { children: React.ReactNode}) {
  const [breadCrumbs, setBreadCrumbs] = useState<React.ReactNode[]>([])
  const [portalList, setPortalList] = useState<Portal[]>([])


  const navState: NavContextT = {
    breadCrumbs, setBreadCrumbs, portalList, setPortalList
  }

  return <NavContext.Provider value={navState}>
    { children }
  </NavContext.Provider>
}
