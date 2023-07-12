import React, {useContext} from 'react'

export type ImageUrlFunc = (cleanFileName: string, version: number) => string
export type SubmitMailingListContactFunc = (name: string, email: string) => Promise<object>
export type ApiContextT = {
  getImageUrl: ImageUrlFunc,
  submitMailingListContact: SubmitMailingListContactFunc
}

export const previewApi: ApiContextT = {
  getImageUrl: () => '',
  submitMailingListContact: () => Promise.resolve({})
}

const ApiContext = React.createContext<ApiContextT>(previewApi)
export const useApiContext = () => {
  return useContext(ApiContext)
}

export const ApiProvider = ({api, children}: {api: ApiContextT, children: React.ReactNode}) => {
  return <ApiContext.Provider value={api}>
    {children}
  </ApiContext.Provider>
}
