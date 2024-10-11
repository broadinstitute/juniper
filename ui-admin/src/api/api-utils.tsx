import React, { useEffect, useState } from 'react'
import { failureNotification } from 'util/notifications'
import { Store } from 'react-notifications-component'

export type ApiErrorResponse = {
  message: string,
  error?: string
  statusCode: number
}

const errorSuffix = 'If this error persists, please contact support@juniper.terra.bio'

/**
 * performs default error message alerting if an error occurs during an API request.
 * shows a specific error message if the error is auth-related.
 */
export const defaultApiErrorHandle = (error: ApiErrorResponse,
  errorHeader = 'An unexpected error occurred. ') => {
  if (error.statusCode === 401) {
    Store.addNotification(failureNotification(<div>
      <div>{errorHeader}</div>
      <div>Request could not be authenticated
                -- you may need to log in again </div>
      <div>{errorSuffix}</div>
    </div>
    ))
  } else if (error.statusCode === 403) {
    Store.addNotification(failureNotification(<div>
      <div>Permission denied: {error.message}</div>
      <div>If this is unexpected, you may need to log out and log in again to refresh your credentials.</div>
    </div>
    ))
  } else {
    Store.addNotification(failureNotification(<div>
      <div>{errorHeader}</div>
      <div>{error.message ?? error.error}</div>
      <div>{errorSuffix}</div>
    </div>
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
  return { isLoading, isError, reload, setIsLoading }
}

/**
 * utility function for wrapping an Api call in loading and error handling
 */
export const doApiLoad = async (loadingFunc: () => Promise<unknown>,
  opts: {
        setIsLoading?: (isLoading: boolean) => void,
        setIsError?: (isError: boolean) => void,
        customErrorMsg?: string,
        alertErrors?: boolean,
        setError?: (error: string) => void
    } = {}) => {
  if (opts.alertErrors === undefined) {
    opts.alertErrors = true
  }
  if (opts.setIsLoading) { opts.setIsLoading(true) }
  try {
    await loadingFunc()
    if (opts.setIsError) { opts.setIsError(false) }
  } catch (e) {
    if (opts.alertErrors) { defaultApiErrorHandle(e as ApiErrorResponse, opts.customErrorMsg) }
    if (opts.setIsError) { opts.setIsError(true) }
    if (opts.setError) { opts.setError((e as ApiErrorResponse).message) }
  }
  if (opts.setIsLoading) { opts.setIsLoading(false) }
}
