import React, { useContext } from 'react'

export type ImageUrlFunc = (cleanFileName: string, version: number) => string
export type SubmitMailingListContactFunc = (name: string, email: string) => Promise<object>
export type GetLanguageTextsFunc = (selectedLanguage: string, portalShortcode?: string) =>
    Promise<Record<string, string>>

/**
 * represents a minimal set of api functions needed to make the participant ui functional outside of the
 * main participant ui app.
 */
export type ApiContextT = {
  getImageUrl: ImageUrlFunc,
  submitMailingListContact: SubmitMailingListContactFunc,
  getLanguageTexts: GetLanguageTextsFunc
}

export const emptyApi: ApiContextT = {
  getImageUrl: () => '',
  submitMailingListContact: () => Promise.resolve({}),
  getLanguageTexts: () => Promise.resolve({})
}

const ApiContext = React.createContext<ApiContextT>(emptyApi)
/** helper function for using the api context */
export const useApiContext = () => {
  return useContext(ApiContext)
}

/**
 * provider for an Api object -- this enables the same participant UI components to be plugged into different API
 * endpoints depending on whether they are being rendered in the participant UI, or in preview-mode on the admin tool
 * Unlike many other providers, this does not do anything asynchronously or make server requests, it's just a container
 */
export const ApiProvider = ({ api, children }: {api: ApiContextT, children: React.ReactNode}) => {
  return <ApiContext.Provider value={api}>
    {children}
  </ApiContext.Provider>
}
