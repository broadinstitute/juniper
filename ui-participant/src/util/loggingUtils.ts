import Api, { getEnvSpec } from 'api/api'

/** listens to all window errors and logs them  */
const setupErrorLogger = () => {
  window.addEventListener('error', event => {
    const envSpec = getEnvSpec()
    Api.log({
      eventType: 'ERROR',
      eventName: 'jserror',
      portalShortcode: envSpec.shortcode ?? envSpec.shortcodeOrHostname,
      // TODO figure out how to get username and studyShortcode here
      environmentName: envSpec.envName,
      // TODO adding browser agent stuff will likely be helpful
      eventDetail: JSON.stringify({ message: event.error.message }),
      stackTrace: event.error.stack
    })
  })
}

export default setupErrorLogger

/** logs web vitals from reportWebVitals */
export const logVitals = (metric: object) => {
  const envSpec = getEnvSpec()
  Api.log({
    eventType: 'STATS',
    eventName: 'webvitals',
    portalShortcode: envSpec.shortcode ?? envSpec.shortcodeOrHostname,
    environmentName: envSpec.envName,
    eventDetail: JSON.stringify(metric)
  })
}
