import Api, { getEnvSpec, LogEvent } from 'api/api'

/** listens to all window errors and logs them  */
const setupErrorLogger = () => {
  window.addEventListener('error', event => {
    logError({ message: event.error.message }, event.error.stack)
  })
}

export default setupErrorLogger

/** logs web vitals from reportWebVitals */
export const logVitals = (metric: object) => {
  log({
    eventType: 'STATS',
    eventName: 'webvitals',
    eventDetail: JSON.stringify(metric)
  })
}

/**
 * logs an event to the server. This takes care of setting the portalShortcode and environmentName on the event
 * in the future, this should handle getting username and studyShortcode here and/or browser agent stuff
 */
export const log = (event: LogEvent) => {
  const envSpec = getEnvSpec()
  event.portalShortcode = envSpec.shortcode ?? envSpec.shortcodeOrHostname
  event.environmentName = envSpec.envName
  Api.log(event)
}

export type ErrorEventDetail = {
  message: string,
  responseCode?: number
}

export const logError = (detail: ErrorEventDetail, stackTrace: string) => {
  Api.log({
    eventType: 'ERROR',
    eventName: 'jserror',
    eventDetail: JSON.stringify(detail),
    stackTrace
  })
}
