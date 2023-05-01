import React, { Dispatch, SetStateAction, useState } from 'react'
import BaseSidebar from './BaseSidebar'

export type NavbarContextT = {
  breadCrumbs: React.ReactNode[],
  sidebarContent: React.ReactNode | null,
  showSidebar: boolean,
  setShowSidebar: Dispatch<SetStateAction<boolean>>,
  setSidebarContent: Dispatch<SetStateAction<React.ReactNode>>,
  setBreadCrumbs: Dispatch<SetStateAction<React.ReactNode[]>>
}

const emptyNavbarContext: NavbarContextT = {
  breadCrumbs: [],
  sidebarContent: null,
  showSidebar: false,
  setShowSidebar: () => alert('error - navbar not initialized'),
  setSidebarContent: () => alert('error - navbar not initialized'),
  setBreadCrumbs: () => alert('error - navbar not initialized')
}

export const NavbarContext = React.createContext<NavbarContextT>(emptyNavbarContext)

/** Provider for a navbar context (does not actually render the navbar) */
export default function NavbarProvider({ children }: { children: React.ReactNode}) {
  const [breadCrumbs, setBreadCrumbs] = useState<React.ReactNode[]>([])
  const [showSidebar, setShowSidebar] = useState(false)
  const [sidebarContent, setSidebarContent] = useState<React.ReactNode>(<BaseSidebar setShow={setShowSidebar}/>)


  const navState: NavbarContextT = {
    breadCrumbs, setBreadCrumbs, sidebarContent, setSidebarContent, showSidebar, setShowSidebar
  }

  return <NavbarContext.Provider value={navState}>
    { children }
  </NavbarContext.Provider>
}
