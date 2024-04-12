import React, { useState } from 'react'
import Api from 'api/api'
import LoadingSpinner from 'util/LoadingSpinner'
import { Button } from 'components/forms/Button'
import { doApiLoad } from 'api/api-utils'
import { successNotification } from '../util/notifications'
import { Store } from 'react-notifications-component'

/** control for executing ad-hoc populate commands */
export default function PopulateCommand() {
  const [isLoading, setIsLoading] = useState(false)
  const [command, setCommand] = useState('')
  const [result, setResult] = useState<object>()
  const doCommand = async () => {
    doApiLoad(async () => {
      const response = await Api.populateCommand(command, {})
      setResult(response)
      Store.addNotification(successNotification('Command executed'))
    }, { setIsLoading })
  }

  return <form onSubmit={e => {
    e.preventDefault()
    if (!isLoading) { doCommand() }
  }}>
    <h3>Execute populate command</h3>
    <label className="form-label">
      Command
      <input type="text" value={command} className="form-control"
        onChange={e => setCommand(e.target.value)}/>
    </label>
    { !isLoading && !!result && <div className="mt-5">
      <h5>Result</h5>
      { JSON.stringify(result, null, 2) }
    </div> }
    <br/>
    <Button variant="primary" type="button" onClick={doCommand} disabled={isLoading}>
      {isLoading ? <LoadingSpinner/> : 'Execute'}
    </Button>
  </form>
}
