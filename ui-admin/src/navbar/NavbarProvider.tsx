import React, { useState } from 'react'

export type NavbarContextT = {
  menuContent: React.ReactNode | null,
  sidebarContent: React.ReactNode | null,
  showSidebar: boolean,
  setShowSidebar: (showSidebar: boolean) => void,
  setSidebarContent: (sidebarContent: React.ReactNode) => void,
  setMenuContent: (menuContent: React.ReactNode) => void
}

const emptyNavbarContext = {
  menuContent: null,
  sidebarContent: null,
  showSidebar: false,
  setShowSidebar: () => alert('error - navbar not initialized'),
  setSidebarContent: () => alert('error - navbar not initialized'),
  setMenuContent: () => alert('error - navbar not initialized')
}

export const NavbarContext = React.createContext<NavbarContextT>(emptyNavbarContext)

/** Provider for a navbar context (does not actually render the navbar) */
export default function NavbarProvider({ children }: { children: React.ReactNode}) {
  const [menuContent, setMenuContent] = useState<React.ReactNode>(null)
  const [sidebarContent, setSidebarContent] = useState<React.ReactNode>(null)
  const [showSidebar, setShowSidebar] = useState(false)

  const navState: NavbarContextT = {
    menuContent, setMenuContent, sidebarContent, setSidebarContent, showSidebar, setShowSidebar
  }

  return <NavbarContext.Provider value={navState}>
    { children }
  </NavbarContext.Provider>
}
