import React from 'react'
import { useSearchParams } from 'react-router-dom'

export type GlobalSearchParamsProviderT = {
  searchParams: URLSearchParams
  setSearchParams: React.Dispatch<URLSearchParams>
}


export const GlobalSearchParamsContext = React.createContext<GlobalSearchParamsProviderT | undefined>(undefined)

export const GlobalSearchParamsProvider = ({ children }: { children: React.ReactNode }) => {
  const [searchParams, setSearchParams] = useSearchParams()

  return (
    <GlobalSearchParamsContext.Provider value={{
      searchParams,
      setSearchParams
    }}>
      {children}
    </GlobalSearchParamsContext.Provider>
  )
}

export const useGlobalSearchParams = () => {
  const context = React.useContext(GlobalSearchParamsContext)
  if (!context) {
    throw new Error('useGlobalSearchParams must be used within a GlobalSearchParamsProvider')
  }
  return context
}
