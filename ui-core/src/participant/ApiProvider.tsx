import React, {useContext} from 'react'

export type ApiContextT = {
  getImageUrl: (cleanFileName: string, version: number) => string
}

export const previewApi: ApiContextT = {
  getImageUrl: () => ''
}

const ApiContext = React.createContext<ApiContextT>(previewApi)
export const useApiContext = () => {
  return useContext(ApiContext)
}

export function ApiProvider({api, children}: {api: ApiContextT, children: React.ReactNode}) {
  return <ApiContext.Provider value={api}>
    {children}
  </ApiContext.Provider>
}
