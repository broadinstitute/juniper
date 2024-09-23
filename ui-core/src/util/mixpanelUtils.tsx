import mixpanel from 'mixpanel-browser'

export const initializeMixpanel = (token: string) => {
  mixpanel.init(token, {
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
}
