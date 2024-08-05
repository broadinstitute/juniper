import { useEffect } from 'react'
import mixpanel from 'mixpanel-browser'

const useMixpanel = (token: string) => {
  useEffect(() => {
    mixpanel.init(token, {
      'debug': false,
      'track_pageview': 'url-with-path',
      'persistence': 'localStorage',
      'api_payload_format': 'json',
      'api_host': `https://${window.location.host}`,
      'api_routes': {
        track: 'api/public/log/v1/track',
        // The following two routes are API stubs that are put in
        // place to prevent the Mixpanel library from throwing errors
        engage: 'api/public/log/v1/engage',
        groups: 'api/public/log/v1/groups'
      }
    })
  }, [token])

  const logEvent = (eventName: string, properties?: Record<string, string>) => {
    mixpanel.track(eventName, properties)
  }

  return { logEvent }
}

export default useMixpanel
