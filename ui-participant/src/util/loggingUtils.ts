import Api, { getEnvSpec } from 'api/api'
// we use flatted here since we don't control the objects we're serializing, and so we need to be more
// robust to circular references than JSON.stringify
import stringify from 'json-stringify-safe'
import { isBrowserCompatible } from './browserCompatibilityUtils'
import mixpanel from 'mixpanel-browser'
import { LogEvent } from '@juniper/ui-core'

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
export const logToDatabase = (event: LogEvent) => {
  const envSpec = getEnvSpec()
  event.portalShortcode = envSpec.shortcode ?? envSpec.shortcodeOrHostname
  event.environmentName = envSpec.envName
  Api.log(event)
}

export const logToMixpanel = (event: LogEvent) => {
  const envSpec = getEnvSpec()
  event.portalShortcode = envSpec.shortcode ?? envSpec.shortcodeOrHostname
  event.environmentName = envSpec.envName
  mixpanel.track(event.eventName, {
    eventName: event.eventName,
    eventType: event.eventType,
    eventDetail: event.eventDetail,
    stackTrace: event.stackTrace,
    portalShortcode: event.portalShortcode,
    environmentName: event.environmentName
  })
}

/** logs an event to the server and mixpanel
 *  Logging to both places is temporary until we've fully transitioned to mixpanel
 */
export const log = (event: LogEvent) => {
  logToDatabase(event)
  logToMixpanel(event)
}

export type ErrorEventDetail = {
  message: string,
  responseCode?: number
}

/** specific helper function for logging an error */
export const logError = (detail: ErrorEventDetail, stackTrace: string | undefined, eventName= 'jserror') => {
  if (!isBrowserCompatible()) {
    alert('Your browser does not support this page. ' +
      'Please use the latest version of Chrome, Safari, Firefox, Edge, or Android')
    log({
      eventType: 'INFO', eventName: 'js-compatibility',
      eventDetail: detail.message
    })
  } else {
    log({
      eventType: 'ERROR',
      eventName,
      eventDetail: `${stringify(detail)}\n${window.location.href}`,
      stackTrace
    })
  }
}
