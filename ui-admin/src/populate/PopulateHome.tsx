import React, { useState } from 'react'
import { doApiLoad } from '../api/api-utils'
import Api from '../api/api'
import { Store } from 'react-notifications-component'
import { successNotification } from '../util/notifications'
import { Button } from '../components/forms/Button'
import LoadingSpinner from '../util/LoadingSpinner'

/**
 * Shows populate controls we expect non-engineers to be able to use
 */
export default function PopulateHome() {
  const [isLoading, setIsLoading] = useState(false)

  const refreshDemo = async () => {
    if (confirm('Reset demo portal?')) {
      doApiLoad(async () => {
        await Api.populatePortal('portals/demo/portal.json', true, undefined)
        Store.addNotification(successNotification('Populate succeeded'))
        // redirect to home with a hard reload to capture the new portal
        window.location.pathname = '/'
      }, { setIsLoading })
    }
  }

  return <div>
    <h3 className="h5">Demo</h3>
    <div>
      <Button variant="primary" onClick={refreshDemo} disabled={isLoading}>
        {isLoading ? <LoadingSpinner/> : 'Refresh demo' }
      </Button>
    </div>
  </div>
}
