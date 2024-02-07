import Api, { getEnvSpec, LogEvent } from 'api/api'
// we use flatted here since we don't control the objects we're serializing, and so we need to be more
// robust to circular references than JSON.stringify
import stringify from 'json-stringify-safe'

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
    eventDetail: stringify(metric)
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

/** specific helper function for logging an error */
export const logError = (detail: ErrorEventDetail, stackTrace: string) => {
  if (detail.message.startsWith('Object.hasOwn is not')) {
    alert('Your browser does not support this page. ' +
      'Please use the latest version of Chrome, Safari, Firefox, Edge, or Android')
    log({
      eventType: 'INFO', eventName: 'js-compatibility',
      eventDetail: detail.message
    })
  } else {
    log({
      eventType: 'ERROR',
      eventName: 'jserror',
      eventDetail: `${stringify(detail)}\n${window.location.href}`,
      stackTrace
    })
  }
}
