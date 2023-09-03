import { failureNotification } from './notifications'
import { Store } from 'react-notifications-component'
import { useEffect, useState } from 'react'

export type ApiErrorResponse = {
    message: string,
    statusCode: number
}

const errorSuffix = 'If this error persists, please contact support@juniper.terra.bio'

/**
 * performs default error message alerting if an error occurs during an API request.
 * shows a specific error message if the error is auth-related.
 */
export const defaultApiErrorHandle = (error: ApiErrorResponse,
  errorHeader = 'An unexpected error occurred. ') => {
  if (error.statusCode === 401 || error.statusCode === 403) {
    Store.addNotification(failureNotification(
            `${errorHeader}\n\nRequest could not be authorized
             -- you may need to log in again\n\n${errorSuffix}`
    ))
  } else {
    Store.addNotification(failureNotification(
            `${errorHeader}\n\n${error.message}\n\n${errorSuffix}`
    ))
  }
}

/**
 * utility effect for components that want to load something from the API on first render.
 * returns loading and error state, as well as a function that can be called to reload.
 */
export const useLoadingEffect = (loadingFunc: () => Promise<unknown>,
  deps: unknown[] = [], customErrorMsg?: string) => {
  const [isLoading, setIsLoading] = useState(true)
  const [isError, setIsError] = useState(false)
  const reload = () => doApiLoad(loadingFunc, { setIsError, customErrorMsg, setIsLoading })

  useEffect(() => {
    reload()
  }, deps)
  return { isLoading, isError, reload }
}

/**
 * utility function for wrapping an Api call in loading and error handling
 */
export const doApiLoad = async (loadingFunc: () => Promise<unknown>,
  opts: {
        setIsLoading?: (isLoading: boolean) => void,
        setIsError?: (isError: boolean) => void,
        customErrorMsg?: string
    } = {}) => {
  if (opts.setIsLoading) { opts.setIsLoading(true) }
  try {
    await loadingFunc()
    if (opts.setIsError) { opts.setIsError(false) }
  } catch (e) {
    defaultApiErrorHandle(e as ApiErrorResponse, opts.customErrorMsg)
    if (opts.setIsError) { opts.setIsError(true) }
  }
  if (opts.setIsLoading) { opts.setIsLoading(false) }
}
