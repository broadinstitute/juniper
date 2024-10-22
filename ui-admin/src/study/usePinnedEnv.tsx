import React, { createContext, useContext, useState, ReactNode } from 'react'

interface PinnedEnvContextProps {
    pinnedEnv?: string
    setPinnedEnv: (env?: string) => void
}

const PinnedEnvContext = createContext<PinnedEnvContextProps | undefined>(undefined)

export const PinnedEnvProvider = ({ children }: { children: ReactNode }) => {
  const [pinnedEnv, setPinnedEnv] = useState<string>()

  return (
    <PinnedEnvContext.Provider value={{ pinnedEnv, setPinnedEnv }}>
      {children}
    </PinnedEnvContext.Provider>
  )
}

export const usePinnedEnv = (): PinnedEnvContextProps => {
  const context = useContext(PinnedEnvContext)
  if (!context) {
    throw new Error('usePinnedEnv must be used within a PinnedEnvProvider')
  }
  return context
}
