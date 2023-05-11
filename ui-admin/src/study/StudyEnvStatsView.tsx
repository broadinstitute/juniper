import React, { useEffect, useState } from 'react'
import { StudyEnvContextT } from './StudyEnvironmentRouter'
import Api, { StudyEnvStats } from 'api/api'
import { failureNotification } from 'util/notifications'
import { Store } from 'react-notifications-component'
import LoadingSpinner from 'util/LoadingSpinner'

/** shows summary stats for the study.  very simple for now--this will eventually have charts and graphs */
export default function StudyEnvStatsView({ studyEnvContext }: {studyEnvContext: StudyEnvContextT}) {
  const [stats, setStats] = useState<StudyEnvStats | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  useEffect(() => {
    setIsLoading(true)
    Api.fetchStats(studyEnvContext.portal.shortcode, studyEnvContext.study.shortcode,
      studyEnvContext.currentEnv.environmentName).then(result => {
      setStats(result)
      setIsLoading(false)
    }).catch(e => {
      Store.addNotification(failureNotification(e.message))
    })
  }, [])
  return <div className="container row">
    <div className="col-12 p-4">
      <h1 className="h4">{studyEnvContext.study.name} Summary
        <span className="fst-italic text-muted ms-3">({studyEnvContext.currentEnv.environmentName})</span>
      </h1>
      <LoadingSpinner isLoading={isLoading}/>
      {(!isLoading && !!stats) && <ul>
        <li>Active Enrollees: {stats.enrolleeCount} </li>
        <li>Withdrawn Enrollees: {stats.withdrawnCount} </li>
      </ul> }
    </div>
  </div>
}
