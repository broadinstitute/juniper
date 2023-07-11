import { logError } from './loggingUtils'

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
    alert(`${errorHeader}\n\nRequest could not be authorized -- you may need to log in again\n\n${errorSuffix}`)
  } else {
    alert(`${errorHeader}\n\n${error.message}\n\n${errorSuffix}`)
  }
  logError({
    message: error.message,
    responseCode: error.statusCode
  }, Error().stack ?? '')
}
